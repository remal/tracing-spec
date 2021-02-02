package name.remal.tracingspec.retriever.zipkin.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import name.remal.tracingspec.retriever.zipkin.internal.ImmutableZipkinSpan.ZipkinSpanBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Value.Immutable
@Gson.TypeAdapters
@JsonDeserialize(builder = ZipkinSpanBuilder.class)
@JsonInclude(NON_ABSENT)
public interface ZipkinSpan {

    static ZipkinSpanBuilder builder() {
        return ImmutableZipkinSpan.builder();
    }


    String getId();

    Optional<String> getParentId();

    Optional<ZipkinSpanKind> getKind();

    Optional<String> getName();

    OptionalLong getTimestamp();

    @Default
    default Map<String, String> getTags() {
        return emptyMap();
    }

    @Default
    default List<ZipkinSpanAnnotation> getAnnotations() {
        return emptyList();
    }

    Optional<ZipkinSpanEndpoint> getLocalEndpoint();

    Optional<ZipkinSpanEndpoint> getRemoteEndpoint();

}
