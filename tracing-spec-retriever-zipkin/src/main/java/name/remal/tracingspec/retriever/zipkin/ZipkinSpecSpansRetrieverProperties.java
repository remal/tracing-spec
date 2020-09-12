/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    long connectTimeoutMillis = 5_000;

    /**
     * Write timeout in milliseconds
     */
    @Min(1)
    long writeTimeoutMillis = 10_000;

    /**
     * Read timeout in milliseconds
     */
    @Min(1)
    long readTimeoutMillis = 60_000;


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
