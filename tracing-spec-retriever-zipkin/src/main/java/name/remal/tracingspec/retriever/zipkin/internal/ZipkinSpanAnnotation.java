package name.remal.tracingspec.retriever.zipkin.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import java.util.OptionalLong;
import name.remal.tracingspec.retriever.zipkin.internal.ImmutableZipkinSpanAnnotation.ZipkinSpanAnnotationBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Value.Immutable
@Gson.TypeAdapters
@JsonDeserialize(builder = ZipkinSpanAnnotationBuilder.class)
@JsonInclude(NON_ABSENT)
public interface ZipkinSpanAnnotation {

    static ZipkinSpanAnnotationBuilder builder() {
        return ImmutableZipkinSpanAnnotation.builder();
    }


    OptionalLong getTimestamp();

    Optional<String> getValue();

}
