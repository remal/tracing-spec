package name.remal.tracingspec.spring;

import static org.springframework.aop.support.AopUtils.getMostSpecificMethod;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import brave.Tracer;
import com.google.common.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.api.RelocateClasses;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

@Internal
@RelocateClasses(TypeToken.class)
@SuppressWarnings({"UnstableApiUsage", "java:S2160", "java:S1948"})
abstract class AbstractAnnotationPointcutAdvisor<A extends Annotation> extends AbstractPointcutAdvisor {

    @Nullable
    @OverrideOnly
    protected Predicate<A> getHiddenGetter() {
        return null;
    }

    @Nullable
    @OverrideOnly
    protected Function<A, String> getKindGetter() {
        return null;
    }

    @Nullable
    @OverrideOnly
    protected Predicate<A> getAsyncGetter() {
        return null;
    }

    @Nullable
    @OverrideOnly
    protected Function<A, String> getServiceNameGetter() {
        return null;
    }

    @Nullable
    @OverrideOnly
    protected Function<A, String> getRemoteServiceNameGetter() {
        return null;
    }

    @Nullable
    @OverrideOnly
    protected Function<A, String> getDescriptionGetter() {
        return null;
    }


    private final ObjectProvider<Tracer> tracer;
    private final TracingSpecSpringProperties properties;

    private final Class<A> annotationType;

    @Nullable
    private final Predicate<A> hiddenGetter;
    @Nullable
    private final Function<A, String> kindGetter;
    @Nullable
    private final Predicate<A> asyncGetter;
    @Nullable
    private final Function<A, String> serviceNameGetter;
    @Nullable
    private final Function<A, String> remoteServiceNameGetter;
    @Nullable
    private final Function<A, String> descriptionGetter;

    private final Pointcut pointcut;
    private final MethodInterceptor advice;

    protected AbstractAnnotationPointcutAdvisor(ObjectProvider<Tracer> tracer, TracingSpecSpringProperties properties) {
        this.tracer = tracer;
        this.properties = properties;

        this.annotationType = getAnnotationType();

        this.hiddenGetter = getHiddenGetter();
        this.kindGetter = getKindGetter();
        this.asyncGetter = getAsyncGetter();
        this.serviceNameGetter = getServiceNameGetter();
        this.remoteServiceNameGetter = getRemoteServiceNameGetter();
        this.descriptionGetter = getDescriptionGetter();

        this.pointcut = new AnnotationMatchingPointcut(null, this.annotationType, true);
        this.advice = new DescriptionAnnotationAdvice();
    }

    @SuppressWarnings("unchecked")
    private Class<A> getAnnotationType() {
        val advisorType = TypeToken.of(getClass())
            .getSupertype(AbstractAnnotationPointcutAdvisor.class)
            .getType();
        if (!(advisorType instanceof ParameterizedType)) {
            throw new IllegalStateException(advisorType + " is not an instance of ParameterizedType");
        }
        val advisorParameterizedType = (ParameterizedType) advisorType;
        return (Class<A>) TypeToken.of(advisorParameterizedType.getActualTypeArguments()[0]).getRawType();
    }

    @Override
    public final Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public final MethodInterceptor getAdvice() {
        return advice;
    }


    private class DescriptionAnnotationAdvice implements IntroductionInterceptor, Ordered {

        @Override
        @Nullable
        @SneakyThrows
        @SuppressWarnings("java:S3776")
        public Object invoke(MethodInvocation invocation) {
            if (!properties.isEnabled()) {
                return invocation.proceed();
            }

            val span = tracer.getObject().currentSpan();
            if (span == null || span.isNoop()) {
                return invocation.proceed();
            }

            if (properties.isDescriptionOnlyIfDebug() && !span.context().debug()) {
                return invocation.proceed();
            }

            A annotation = Optional.ofNullable(invocation.getMethod())
                .map(it -> getMostSpecificMethod(it, invocation.getThis().getClass()))
                .map(it -> findAnnotation(it, annotationType))
                .orElse(null);
            if (annotation == null) {
                return invocation.proceed();
            }

            if (hiddenGetter != null) {
                val hidden = hiddenGetter.test(annotation);
                if (hidden) {
                    span.tag("spec.hidden", "1");
                }
            }

            if (kindGetter != null) {
                val kind = kindGetter.apply(annotation);
                if (kind != null && !kind.isEmpty()) {
                    span.tag("spec.kind", kind);
                }
            }

            if (asyncGetter != null) {
                val async = asyncGetter.test(annotation);
                if (async) {
                    span.tag("spec.async", "1");
                }
            }

            if (serviceNameGetter != null) {
                val serviceName = serviceNameGetter.apply(annotation);
                if (serviceName != null && !serviceName.isEmpty()) {
                    span.tag("spec.serviceName", serviceName);
                }
            }

            if (remoteServiceNameGetter != null) {
                val remoteServiceName = remoteServiceNameGetter.apply(annotation);
                if (remoteServiceName != null && !remoteServiceName.isEmpty()) {
                    span.tag("spec.remoteServiceName", remoteServiceName);
                }
            }

            if (descriptionGetter != null) {
                val description = descriptionGetter.apply(annotation);
                if (description != null && !description.isEmpty()) {
                    span.tag("spec.description", description);
                }
            }

            return invocation.proceed();
        }


        @Override
        public boolean implementsInterface(Class<?> intf) {
            return true;
        }

        @Override
        public int getOrder() {
            return LOWEST_PRECEDENCE;
        }

    }

}
