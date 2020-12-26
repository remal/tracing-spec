package name.remal.tracingspec.renderer;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;
import static org.springframework.beans.BeanUtils.findPrimaryConstructor;
import static org.springframework.core.KotlinDetector.isKotlinPresent;
import static org.springframework.core.KotlinDetector.isKotlinType;
import static org.springframework.core.ResolvableType.forClass;
import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import com.google.common.base.CaseFormat;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.api.RelocateClasses;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.BindConstructorProvider;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.convert.ConversionService;
import org.springframework.validation.BindException;
import org.springframework.validation.Validator;

/**
 * <p>We would like to add {@link SpecSpansGraphProcessor}s and {@link SpecSpanNodeProcessor}s to
 * {@link RenderingOptions} from Spring properties of these formats:</p>
 * <p></p>
 * <p>1. By processor's alias name:</p><pre><code>
 * tracingspec:
 *   renderer:
 *     options:
 *       graph-processors:
 *       - ReplaceSingleRootWithChildren
 * </code></pre>
 * <p></p>
 * <p>2. By processor's alias name plus constructor parameters:</p><pre><code>
 * tracingspec:
 *   renderer:
 *     options:
 *       node-processors:
 *       - js
 *           code: 'node.putTag("name", "value")'
 * </code></pre>
 * <p></p>
 * <p>This can't be done just by using {@link ConfigurationProperties} annotation, that's why we need this
 * {@link BeanPostProcessor}.</p>
 */
@Internal
class RenderingOptionsBeanPostProcessor implements BeanPostProcessor, Ordered {

    private static final Map<String, Class<SpecSpansGraphProcessor>> GRAPH_PROCESSOR_IMPLS = loadImpls(
        SpecSpansGraphProcessor.class,
        "/META-INF/spec-spans-graph-processors.properties"
    );

    private static final Map<String, Class<SpecSpanNodeProcessor>> NODE_PROCESSOR_IMPLS = loadImpls(
        SpecSpanNodeProcessor.class,
        "/META-INF/spec-span-node-processors.properties"
    );

    private static final BindConstructorProvider BIND_CONSTRUCTOR_PROVIDER =
        new ConstructorBindingBindConstructorProvider();


    private final ApplicationContext context;

    private final ObjectProvider<Validator> validatorProvider;

    public RenderingOptionsBeanPostProcessor(ApplicationContext context) {
        this.context = context;
        this.validatorProvider = context.getBeanProvider(Validator.class);
    }

    @Nullable
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof RenderingOptions) {
            val renderingOptions = (RenderingOptions) bean;

            val graphProcessors = getProcessors(GRAPH_PROCESSOR_IMPLS, "graph-processors");
            renderingOptions.addGraphProcessors(graphProcessors);

            val nodeProcessors = getProcessors(NODE_PROCESSOR_IMPLS, "node-processors");
            renderingOptions.addNodeProcessors(nodeProcessors);
        }
        return bean;
    }

    private <T> List<T> getProcessors(Map<String, Class<T>> impls, String propertyName) {
        val configurationProperties = findAnnotation(RenderingOptions.class, ConfigurationProperties.class);
        if (configurationProperties == null) {
            throw new IllegalStateException(format(
                "%s is not annotated with %s",
                RenderingOptions.class,
                ConfigurationProperties.class
            ));
        }
        val rootProperty = configurationProperties.value() + '.' + propertyName;

        List<T> processors = new ArrayList<>();
        for (int index = 0; index < Integer.MAX_VALUE; ++index) {
            val indexProperty = rootProperty + '.' + index;
            T processor = getProcessor(impls, indexProperty);
            if (processor != null) {
                processors.add(processor);
                continue;
            }

            break;
        }

        return processors;
    }

    @Nullable
    @SuppressWarnings({"java:S134", "java:S3776"})
    private <T> T getProcessor(Map<String, Class<T>> impls, String indexProperty) {
        try {
            // Try to initialize the processor by only alias:
            val valueBindResult = getBinder().bind(indexProperty, Object.class);
            if (valueBindResult.isBound()) {
                val value = valueBindResult.get();
                if (value instanceof CharSequence) {
                    val alias = value.toString();
                    val implClass = impls.get(alias);
                    if (implClass == null) {
                        throw new IllegalStateException(format(
                            "Implementation can't be found for %s (property %s)",
                            alias,
                            indexProperty
                        ));
                    }

                    val constructor = implClass.getDeclaredConstructor();
                    makeAccessible(constructor);
                    return validate(constructor.newInstance(), indexProperty);
                }
            }

            // Try to initialize the processor by alias and constructor params:
            BindResult<Map<String, Map<String, Object>>> mapBindResult = getBinder().bind(indexProperty, Bindable.of(
                forClassWithGenerics(
                    Map.class,
                    forClass(String.class),
                    forClassWithGenerics(
                        Map.class,
                        String.class,
                        Object.class
                    )
                )
            ));
            if (mapBindResult.isBound()) {
                val map = mapBindResult.get();
                for (val alias : map.keySet()) {
                    val implClass = impls.get(alias);
                    if (implClass == null) {
                        throw new IllegalStateException(format(
                            "Implementation can't be found for %s (property %s)",
                            alias,
                            indexProperty
                        ));
                    }

                    val paramsProperty = indexProperty + '.' + fromUpperCamelToLowerHyphen(alias);
                    val implBindResult = getBinder().bind(paramsProperty, implClass);
                    if (implBindResult.isBound()) {
                        return validate(implBindResult.get(), indexProperty);
                    }
                }
            }

            return null;

        } catch (Throwable exception) {
            throw new IllegalStateException(
                "Processor can't be initialized from Spring property: " + indexProperty,
                exception
            );
        }
    }

    @Nullable
    private Binder binder;

    private synchronized Binder getBinder() {
        if (this.binder == null) {
            val environment = context.getEnvironment();
            val sources = ConfigurationPropertySources.get(environment);
            val placeholdersResolver = new PropertySourcesPlaceholdersResolver(environment);
            val conversionService = context.getBeanProvider(ConversionService.class).getIfAvailable();
            final Consumer<PropertyEditorRegistry> propertyEditorInitializer;
            if (context instanceof ConfigurableApplicationContext) {
                val beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
                propertyEditorInitializer = beanFactory::copyRegisteredEditorsTo;
            } else {
                propertyEditorInitializer = null;
            }
            this.binder = new Binder(
                sources,
                placeholdersResolver,
                conversionService,
                propertyEditorInitializer,
                null,
                BIND_CONSTRUCTOR_PROVIDER
            );
        }
        return requireNonNull(this.binder);
    }

    @Contract("_, _ -> param1")
    @SneakyThrows
    private <T> T validate(T object, String property) {
        val validator = validatorProvider.getIfAvailable();
        if (validator != null) {
            if (validator.supports(object.getClass())) {
                val errors = new BindException(object, property);
                validator.validate(object, errors);
                if (errors.hasErrors()) {
                    throw errors;
                }
            }
        }
        return object;
    }

    @RelocateClasses(CaseFormat.class)
    private static String fromUpperCamelToLowerHyphen(String string) {
        return UPPER_CAMEL.to(LOWER_HYPHEN, string);
    }


    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }


    @SneakyThrows
    @SuppressWarnings({"java:S134", "unchecked"})
    private static <T> Map<String, Class<T>> loadImpls(
        Class<T> type,
        @Language("file-reference") String resourceName
    ) {
        val classLoader = RenderingOptionsBeanPostProcessor.class.getClassLoader();
        if (classLoader == null) {
            return emptyMap();
        }

        Map<String, Class<T>> result = new LinkedHashMap<>();

        while (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        val urls = classLoader.getResources(resourceName);
        while (urls.hasMoreElements()) {
            val url = urls.nextElement();
            try (val inputStream = url.openStream()) {
                val properties = new Properties();
                properties.load(inputStream);
                for (val alias : properties.stringPropertyNames()) {
                    if (result.containsKey(alias)) {
                        continue;
                    }
                    val impl = properties.getProperty(alias);
                    if (alias.isEmpty() || impl == null || impl.isEmpty()) {
                        continue;
                    }

                    val implClass = (Class<T>) Class.forName(impl, false, classLoader);
                    if (!type.isAssignableFrom(implClass)) {
                        throw new IllegalStateException(format(
                            "%s is not %s: %s",
                            implClass,
                            type.getSimpleName(),
                            url
                        ));
                    }

                    result.put(alias, implClass);
                }
            }
        }

        return unmodifiableMap(result);
    }

    private static class ConstructorBindingBindConstructorProvider implements BindConstructorProvider {
        @Override
        @Nullable
        public Constructor<?> getBindConstructor(Bindable<?> bindable, boolean isNestedConstructorBinding) {
            val type = bindable.getType().resolve();
            if (type == null || type.isPrimitive()) {
                return null;
            }

            if (isKotlinPresent() && isKotlinType(type)) {
                val ctor = findPrimaryConstructor(type);
                if (ctor != null) {
                    return ctor;
                }
            }

            val publicCtor = getMatchingConstructor(type.getConstructors());
            if (publicCtor != null) {
                return publicCtor;
            }

            return getMatchingConstructor(type.getDeclaredConstructors());
        }

        @Nullable
        private static Constructor<?> getMatchingConstructor(Constructor<?>... constructors) {
            if (constructors.length == 0) {
                return null;
            } else if (constructors.length == 1) {
                return constructors[0];
            }

            Constructor<?> annotatedCtor = null;
            for (val ctor : constructors) {
                if (ctor.isAnnotationPresent(ConstructorBinding.class)) {
                    if (annotatedCtor != null) {
                        throw new IllegalStateException(
                            annotatedCtor.getDeclaringClass() + " has more than one @ConstructorBinding constructor"
                        );
                    }
                    annotatedCtor = ctor;
                }
            }
            return annotatedCtor;
        }
    }

}
