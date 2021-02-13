package name.remal.tracingspec.spring;

import brave.Tracer;
import io.swagger.annotations.ApiOperation;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.beans.factory.ObjectProvider;

@Internal
class Swagger2PointcutAdvisor extends AbstractAnnotationPointcutAdvisor<ApiOperation> {

    public Swagger2PointcutAdvisor(ObjectProvider<Tracer> tracer, TracingSpecSpringProperties properties) {
        super(tracer, properties);
    }

    @Override
    protected Function<ApiOperation, String> getDescriptionGetter() {
        return ApiOperation::value;
    }

}
