package name.remal.tracingspec.retriever.jaeger.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;
import java.util.Optional;
import name.remal.tracingspec.retriever.jaeger.internal.ImmutableJaegerReference.JaegerReferenceBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Value.Immutable
@Gson.TypeAdapters
@JsonDeserialize(builder = JaegerReferenceBuilder.class)
@JsonInclude(NON_ABSENT)
public interface JaegerReference {

    Optional<JaegerReferenceType> getRefType();

    @JsonProperty("spanID")
    @SerializedName("spanID")
    String getSpanId();

}
