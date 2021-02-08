package name.remal.tracingspec.retriever.jaeger;

import static java.time.temporal.ChronoUnit.MICROS;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.CONSUMER;
import static name.remal.tracingspec.model.SpecSpanKind.PRODUCER;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static name.remal.tracingspec.model.SpecSpanKind.parseSpecSpanKind;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanAnnotation;
import name.remal.tracingspec.model.SpecSpanKind;
import name.remal.tracingspec.retriever.jaeger.internal.JaegerKeyValue;
import name.remal.tracingspec.retriever.jaeger.internal.JaegerReferenceType;
import name.remal.tracingspec.retriever.jaeger.internal.JaegerSpan;
import name.remal.tracingspec.retriever.jaeger.internal.JaegerTrace;
import name.remal.tracingspec.retriever.jaeger.internal.JaegerTraceData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.VisibleForTesting;

@Internal
abstract class JaegerSpanConverter {

    private static final Logger logger = LogManager.getLogger(JaegerSpanConverter.class);

    public static List<SpecSpan> convertJaegerTraceToSpecSpans(JaegerTrace jaegerTrace) {
        return convertJaegerTraceDataItemsToSpecSpans(jaegerTrace.getData());
    }

    private static List<SpecSpan> convertJaegerTraceDataItemsToSpecSpans(List<JaegerTraceData> jaegerTraceDataItems) {
        return jaegerTraceDataItems.stream()
            .flatMap(item -> convertJaegerTraceDataToSpecSpans(item).stream())
            .collect(toList());
    }

    private static List<SpecSpan> convertJaegerTraceDataToSpecSpans(JaegerTraceData jaegerTraceData) {
        Map<String, String> processIdToName = new LinkedHashMap<>();
        jaegerTraceData.getProcesses().forEach((processId, process) ->
            process.getServiceName().filter(name -> !name.isEmpty()).ifPresent(name ->
                processIdToName.put(processId, name)
            )
        );

        return jaegerTraceData.getSpans().stream()
            .map(span -> convertJaegerTraceDataToSpecSpans(span, processIdToName))
            .collect(toList());
    }

    @VisibleForTesting
    static SpecSpan convertJaegerTraceDataToSpecSpans(
        JaegerSpan jaegerSpan,
        Map<String, String> processIdToName
    ) {
        val spanId = processJaegerId(jaegerSpan.getSpanId());
        val specSpan = new SpecSpan(spanId);

        jaegerSpan.getReferences().forEach(ref -> {
            val refSpanId = ref.getSpanId();
            val refType = ref.getRefType().orElse(null);
            if (refType == JaegerReferenceType.CHILD_OF) {
                specSpan.setParentSpanId(processJaegerId(refSpanId));
            } else {
                logger.warn(
                    "Span {}: Unsupported ref type: {}",
                    spanId,
                    refType
                );
            }
        });

        jaegerSpan.getOperationName()
            .filter(it -> !it.isEmpty())
            .ifPresent(specSpan::setName);

        jaegerSpan.getProcessId()
            .flatMap(processId -> Optional.ofNullable(processIdToName.get(processId)))
            .filter(it -> !it.isEmpty())
            .ifPresent(specSpan::setServiceName);

        jaegerSpan.getStartTime().ifPresent(micros ->
            specSpan.setStartedAt(microsecondsToInstant(micros))
        );

        jaegerSpan.getTags().forEach(tag ->
            processKeyValue(spanId, tag, specSpan::putTag)
        );

        jaegerSpan.getLogs().forEach(log ->
            log.getFields().forEach(field ->
                processKeyValue(spanId, field, (key, value) -> {
                    val timestamp = log.getTimestamp();
                    if (timestamp.isPresent()) {
                        val instant = microsecondsToInstant(timestamp.getAsLong());
                        specSpan.addAnnotation(new SpecSpanAnnotation(instant, key, value));

                    } else {
                        specSpan.addAnnotation(new SpecSpanAnnotation(key, value));
                    }
                })
            )
        );

        if (specSpan.getKind() == null) {
            specSpan.setKind(parseSpecSpanKind(specSpan.getTag("span.kind")));
        }

        if (specSpan.getKind() == null) {
            specSpan.getAnnotations().stream()
                .sorted()
                .filter(annotation -> "event".equals(annotation.getKey()))
                .map(SpecSpanAnnotation::getValue)
                .filter(Objects::nonNull)
                .map(EVENT_TO_KIND_MAPPINGS::get)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(specSpan::setKind);
        }

        return specSpan;
    }

    private static String processJaegerId(String id) {
        int padding = id.length() % 16;
        if (padding == 0) {
            return id;
        }
        return "0000000000000000".substring(padding) + id;
    }

    private static void processKeyValue(String spanId, JaegerKeyValue keyValue, BiConsumer<String, String> consumer) {
        val key = keyValue.getKey()
            .filter(it -> !it.isEmpty())
            .orElse(null);
        if (key == null) {
            logger.warn(
                "Span {}: Empty key: {}",
                spanId,
                keyValue
            );
            return;
        }

        keyValue.getValue()
            .map(Object::toString)
            .ifPresent(value ->
                consumer.accept(key, value)
            );
    }


    private static Instant microsecondsToInstant(long micros) {
        val duration = Duration.of(micros, MICROS);
        return Instant.ofEpochSecond(
            duration.getSeconds(),
            duration.getNano()
        );
    }


    private static final Map<String, SpecSpanKind> EVENT_TO_KIND_MAPPINGS;

    static {
        Map<String, SpecSpanKind> eventToKindMappings = new LinkedHashMap<>();
        eventToKindMappings.put("ms", PRODUCER);
        eventToKindMappings.put("mr", CONSUMER);
        eventToKindMappings.put("cs", CLIENT);
        eventToKindMappings.put("sr", SERVER);
        eventToKindMappings.put("ss", SERVER);
        eventToKindMappings.put("cr", CLIENT);
        EVENT_TO_KIND_MAPPINGS = unmodifiableMap(eventToKindMappings);
    }


    private JaegerSpanConverter() {
    }

}
