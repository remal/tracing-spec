package name.remal.tracingspec.model.internal;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.BuilderVisibility;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.jetbrains.annotations.ApiStatus.Internal;

@Target({TYPE, PACKAGE})
@Value.Style(
    defaults = @Value.Immutable(
        copy = false
    ),
    visibility = ImplementationVisibility.SAME,
    builderVisibility = BuilderVisibility.PUBLIC,
    jdkOnly = true,
    get = {"is*", "get*"},
    optionalAcceptNullable = true,
    privateNoargConstructor = true,
    typeBuilder = "*Builder",
    typeInnerBuilder = "BaseBuilder",
    allowedClasspathAnnotations = {
        org.immutables.value.Generated.class,
        Nullable.class,
        Immutable.class,
        ThreadSafe.class,
        NotThreadSafe.class,
    },
    depluralize = true,
    depluralizeDictionary = {
        "child:children",
    }
)
@Internal
public @interface TracingSpecValueStyle {
}
