package name.remal.tracingspec.retriever.jaeger.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.Map;
import name.remal.tracingspec.retriever.jaeger.internal.ImmutableJaegerTraceData.JaegerTraceDataBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Value.Immutable
@Gson.TypeAdapters
@JsonDeserialize(builder = JaegerTraceDataBuilder.class)
@JsonInclude(NON_ABSENT)
public interface JaegerTraceData {

    List<JaegerSpan> getSpans();

    Map<String, JaegerProcess> getProcesses();

}
