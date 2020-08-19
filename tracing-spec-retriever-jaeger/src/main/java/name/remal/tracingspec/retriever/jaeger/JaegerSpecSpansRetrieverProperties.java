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
    private String host;

    /**
     * Jaeger Query service port
     */
    @Min(1)
    @Max(65535)
    private int port = 16686;

    /**
     * Retrieving timeout in milliseconds
     */
    @Min(1)
    private long timeoutMillis = 60_000;

}
