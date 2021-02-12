package name.remal.tracingspec.spring;

import brave.Tracer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;

@Internal
class SpecSpanTagsPointcutAdvisor extends AbstractAnnotationPointcutAdvisor<SpecSpanTags> {

    public SpecSpanTagsPointcutAdvisor(ObjectProvider<Tracer> tracer, TracingSpecSpringProperties properties) {
        super(tracer, properties);
    }

    @Nullable
    @Override
    protected Predicate<SpecSpanTags> getHiddenGetter() {
        return SpecSpanTags::hidden;
    }

    @Nullable
    @Override
    protected Function<SpecSpanTags, String> getKindGetter() {
        return SpecSpanTags::kind;
    }

    @Nullable
    @Override
    protected Predicate<SpecSpanTags> getAsyncGetter() {
        return SpecSpanTags::async;
    }

    @Nullable
    @Override
    protected Function<SpecSpanTags, String> getServiceNameGetter() {
        return SpecSpanTags::serviceName;
    }

    @Nullable
    @Override
    protected Function<SpecSpanTags, String> getRemoteServiceNameGetter() {
        return SpecSpanTags::remoteServiceName;
    }

    @Override
    protected Function<SpecSpanTags, String> getDescriptionGetter() {
        return SpecSpanTags::description;
    }

}
