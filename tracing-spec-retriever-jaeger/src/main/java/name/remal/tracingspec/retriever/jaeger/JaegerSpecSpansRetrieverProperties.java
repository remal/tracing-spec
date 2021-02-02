package name.remal.tracingspec.retriever.jaeger;

import javax.annotation.Nullable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("tracingspec.retriever.jaeger")
@Data
@SuppressWarnings("java:S109")
public class JaegerSpecSpansRetrieverProperties {

    /**
     * Jaeger Query service host
     */
    @Nullable
    @Length(min = 1)
    String host;

    /**
     * Jaeger Query service port
     */
    @Min(1)
    @Max(65535)
    int port = 16686;

    /**
     * Retrieving timeout in milliseconds
     */
    @Min(1)
    long timeoutMillis = 10_000;

}
