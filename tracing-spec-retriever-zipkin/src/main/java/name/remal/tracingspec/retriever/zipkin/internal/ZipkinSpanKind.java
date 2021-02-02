package name.remal.tracingspec.retriever.zipkin.internal;

import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public enum ZipkinSpanKind {
    CLIENT,
    SERVER,
    PRODUCER,
    CONSUMER,
    ;
}
