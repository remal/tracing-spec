package name.remal.tracingspec.retriever.zipkin;

import java.net.URL;
import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Tolerate;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("tracingspec.retriever.zipkin")
@Data
@SuppressWarnings("java:S109")
public class ZipkinSpecSpansRetrieverProperties {

    /**
     * Zipkin URL (for example: http://localhost:9411/)
     */
    @Nullable
    URL url;

    /**
     * Connect timeout in milliseconds
     */
    @Min(1)
    long connectTimeoutMillis = 1_000;

    /**
     * Write timeout in milliseconds
     */
    @Min(1)
    long writeTimeoutMillis = 1_000;

    /**
     * Read timeout in milliseconds
     */
    @Min(1)
    long readTimeoutMillis = 10_000;


    @Tolerate
    @SneakyThrows
    public void setUrl(@Nullable String url) {
        if (url != null) {
            setUrl(new URL(url));
        } else {
            setUrl((URL) null);
        }
    }

}
