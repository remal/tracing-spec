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

package name.remal.tracingspec.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.Data;

@NotThreadSafe
@Data
public class SpecSpan extends SpecSpanInfo<SpecSpan> {

    @JsonProperty(index = 1)
    final String spanId;

    @Nullable
    @JsonProperty(index = 2)
    String parentSpanId;


    public SpecSpan(@JsonProperty("spanId") String spanId) {
        if (spanId.isEmpty()) {
            throw new IllegalArgumentException("spanId must not be empty");
        }
        this.spanId = spanId;
    }


    public void setParentSpanId(@Nullable String parentSpanId) {
        this.parentSpanId = parentSpanId == null || parentSpanId.isEmpty() ? null : parentSpanId;
    }

}
