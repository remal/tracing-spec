package name.remal.tracingspec.retriever.zipkin.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import name.remal.tracingspec.retriever.zipkin.internal.ImmutableZipkinSpanEndpoint.ZipkinSpanEndpointBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Value.Immutable
@Gson.TypeAdapters
@JsonDeserialize(builder = ZipkinSpanEndpointBuilder.class)
@JsonInclude(NON_ABSENT)
public interface ZipkinSpanEndpoint {

    static ZipkinSpanEndpointBuilder builder() {
        return ImmutableZipkinSpanEndpoint.builder();
    }


    Optional<String> getServiceName();

}
