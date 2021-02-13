package name.remal.tracingspec.retriever.jaeger.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import name.remal.tracingspec.retriever.jaeger.internal.ImmutableJaegerSpan.JaegerSpanBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Value.Immutable
@Gson.TypeAdapters
@JsonDeserialize(builder = JaegerSpanBuilder.class)
@JsonInclude(NON_ABSENT)
public interface JaegerSpan {

    @JsonProperty("spanID")
    @SerializedName("spanID")
    String getSpanId();

    Optional<String> getOperationName();

    List<JaegerReference> getReferences();

    OptionalLong getStartTime();

    List<JaegerKeyValue> getTags();

    List<JaegerLog> getLogs();

    @JsonProperty("processID")
    @SerializedName("processID")
    Optional<String> getProcessId();

}
