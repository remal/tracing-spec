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

package name.remal.tracingspec.retriever.zipkin.internal;

import static java.util.Collections.emptyMap;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import name.remal.tracingspec.retriever.zipkin.internal.ImmutableZipkinSpan.ZipkinSpanBuilder;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@Value.Immutable
@Gson.TypeAdapters
public interface ZipkinSpan {

    static ZipkinSpanBuilder builder() {
        return ImmutableZipkinSpan.builder();
    }


    String getId();

    Optional<String> getParentId();

    Optional<String> getName();

    OptionalLong getDuration();

    OptionalLong getTimestamp();

    @Default
    default Map<String, String> getTags() {
        return emptyMap();
    }

    Optional<ZipkinSpanKind> getKind();

    Optional<ZipkinSpanEndpoint> getLocalEndpoint();

    Optional<ZipkinSpanEndpoint> getRemoteEndpoint();

}
