package name.remal.tracingspec.retriever.zipkin;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Instant;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpanAnnotation;
import name.remal.tracingspec.model.SpecSpanKind;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpan;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpanEndpoint;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
interface ZipkinSpanConverter {

    static SpecSpan convertZipkinSpanToSpecSpan(ZipkinSpan zipkinSpan) {
        val specSpan = new SpecSpan(zipkinSpan.getId());

        zipkinSpan.getParentId().ifPresent(specSpan::setParentSpanId);

        specSpan.setName(zipkinSpan.getName().orElse(null));

        specSpan.setKind(zipkinSpan.getKind()
            .map(Object::toString)
            .map(SpecSpanKind::parseSpecSpanKind)
            .orElse(null)
        );

        specSpan.setServiceName(zipkinSpan.getLocalEndpoint()
            .flatMap(ZipkinSpanEndpoint::getServiceName)
            .orElse(null)
        );
        specSpan.setRemoteServiceName(zipkinSpan.getRemoteEndpoint()
            .flatMap(ZipkinSpanEndpoint::getServiceName)
            .orElse(null)
        );

        zipkinSpan.getTimestamp().ifPresent(timestamp ->
            specSpan.setStartedAt(Instant.ofEpochSecond(
                0,
                NANOSECONDS.convert(timestamp, MICROSECONDS)
            ))
        );

        zipkinSpan.getTags().forEach(specSpan::putTag);

        for (val zipkinAnnotation : zipkinSpan.getAnnotations()) {
            val value = zipkinAnnotation.getValue().orElse(null);
            if (value == null) {
                continue;
            }

            if (zipkinAnnotation.getTimestamp().isPresent()) {
                val instant = Instant.ofEpochSecond(
                    0,
                    NANOSECONDS.convert(zipkinAnnotation.getTimestamp().getAsLong(), MICROSECONDS)
                );
                specSpan.addAnnotation(new SpecSpanAnnotation(instant, value));
            } else {
                specSpan.addAnnotation(new SpecSpanAnnotation(value));
            }
        }

        return specSpan;
    }

}
