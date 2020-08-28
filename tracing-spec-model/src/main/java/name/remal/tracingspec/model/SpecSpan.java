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

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.Data;

@NotThreadSafe
@Data
public class SpecSpan implements SpecSpanInfo<SpecSpan> {

    final String spanId;

    @Nullable
    String parentSpanId;

    @Nullable
    String name;

    @Nullable
    SpecSpanKind kind;

    boolean async;

    @Nullable
    String serviceName;

    @Nullable
    String remoteServiceName;

    @Nullable
    Instant startedAt;

    @Nullable
    String description;

    final Map<String, String> tags = new LinkedHashMap<>();

    final Set<SpecSpanAnnotation> annotations = new LinkedHashSet<>();


    @Override
    public boolean isAsync() {
        return async
            || (kind != null && kind.isAlwaysAsync());
    }

}
