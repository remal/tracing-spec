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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import name.remal.tracingspec.model.ImmutableSpecSpan.SpecSpanBuilder;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@Value.Immutable
@JsonDeserialize(builder = SpecSpanBuilder.class)
@JsonInclude(NON_ABSENT)
public interface SpecSpan {

    static SpecSpanBuilder builder() {
        return ImmutableSpecSpan.builder();
    }


    String getSpanId();

    Optional<String> getParentSpanId();

    Optional<String> getLeadingSpanId();

    Optional<String> getName();

    Optional<String> getServiceName();

    Optional<Instant> getStartedAt();

    Optional<Duration> getDuration();

    Optional<String> getDescription();

    @Default
    default boolean isAsync() {
        return false;
    }


    @OverrideOnly
    @Value.Check
    default void validate() {
        if (getSpanId().isEmpty()) {
            throw new IllegalStateException("spanId must not be empty");
        }
        getParentSpanId().ifPresent(parentSpanId -> {
            if (parentSpanId.isEmpty()) {
                throw new IllegalStateException("parentSpanId must not be empty");
            }
        });
        getLeadingSpanId().ifPresent(leadingSpanId -> {
            if (leadingSpanId.isEmpty()) {
                throw new IllegalStateException("leadingSpanId must not be empty");
            }
        });
        getName().ifPresent(name -> {
            if (name.isEmpty()) {
                throw new IllegalStateException("name must not be empty");
            }
        });
        getServiceName().ifPresent(serviceName -> {
            if (serviceName.isEmpty()) {
                throw new IllegalStateException("serviceName must not be empty");
            }
        });
        getDescription().ifPresent(description -> {
            if (description.isEmpty()) {
                throw new IllegalStateException("description must not be empty");
            }
        });
    }

}
