package name.remal.tracingspec.spring;

import brave.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.beans.factory.ObjectProvider;

@Internal
class Swagger3PointcutAdvisor extends AbstractAnnotationPointcutAdvisor<Operation> {

    public Swagger3PointcutAdvisor(ObjectProvider<Tracer> tracer, TracingSpecSpringProperties properties) {
        super(tracer, properties);
    }

    @Override
    protected Function<Operation, String> getDescriptionGetter() {
        return Operation::summary;
    }

}
