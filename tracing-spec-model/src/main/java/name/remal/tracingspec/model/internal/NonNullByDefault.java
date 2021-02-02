package name.remal.tracingspec.model.internal;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import org.jetbrains.annotations.ApiStatus.Internal;

@Nonnull
@TypeQualifierDefault({FIELD, METHOD, PARAMETER})
@Target({PACKAGE, TYPE})
@Retention(RUNTIME)
@Documented
@Internal
public @interface NonNullByDefault {
}
