package name.remal.tracingspec.spring;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.MagicConstant;

@Target({METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Inherited
public @interface SpecSpanTags {

    boolean hidden() default false;

    @MagicConstant(stringValues = {"", "client", "server", "producer", "consumer"})
    String kind() default "";

    boolean async() default false;

    String serviceName() default "";

    String remoteServiceName() default "";

    String description() default "";

}
