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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import static java.lang.Boolean.parseBoolean;
import static java.util.Collections.emptyMap;
import static name.remal.tracingspec.model.SpecSpanTag.DESCRIPTION;
import static name.remal.tracingspec.model.SpecSpanTag.IS_ASYNC;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import name.remal.tracingspec.model.ImmutableSpecSpan.SpecSpanBuilder;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

@Value.Immutable
@JsonDeserialize(builder = SpecSpanBuilder.class)
@JsonInclude(NON_ABSENT)
public interface SpecSpan {

    static SpecSpanBuilder builder() {
        return ImmutableSpecSpan.builder();
    }


    SpecSpanKey getSpanKey();

    Optional<SpecSpanKey> getParentSpanKey();

    Optional<SpecSpanKey> getLeadingSpanKey();

    Optional<String> getName();

    Optional<String> getServiceName();

    Optional<Instant> getStartedAt();

    Optional<Duration> getDuration();

    @Default
    default Map<String, String> getTags() {
        return emptyMap();
    }

    @Default
    @SuppressWarnings("immutables:untype")
    default Optional<String> getDescription() {
        return Optional.ofNullable(getTags().get(DESCRIPTION.getTagName()));
    }

    @Default
    default boolean isAsync() {
        return parseBoolean(getTags().get(IS_ASYNC.getTagName()));
    }

}
