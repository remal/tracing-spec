package name.remal.tracingspec.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("tracingspec.spring")
@Data
@SuppressWarnings("java:S109")
public class TracingSpecSpringProperties {

    /**
     * Is TracingSpec integration with Spring enabled?
     */
    boolean enabled = true;

    /**
     * Add SpecSpan description only if B3 Propagation debug flag is set
     */
    boolean descriptionOnlyIfDebug;

}
