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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
import name.remal.tracingspec.model.ImmutableSpecSpan.SpecSpanBuilder;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@Value.Immutable
@JsonDeserialize(builder = SpecSpanBuilder.class)
public interface SpecSpan extends DisconnectedSpecSpan {

    static SpecSpanBuilder builder() {
        return ImmutableSpecSpan.builder();
    }


    Optional<String> getParentSpanId();

    @JsonIgnore
    default boolean hasParentSpanId() {
        return getParentSpanId().isPresent();
    }

    @JsonIgnore
    default boolean hasNoParentSpanId() {
        return !hasParentSpanId();
    }


    @Override
    @OverrideOnly
    @Value.Check
    default void validate() {
        DisconnectedSpecSpan.super.validate();

        getParentSpanId().ifPresent(parentSpanId -> {
            if (parentSpanId.isEmpty()) {
                throw new IllegalStateException("parentSpanId must not be empty");
            }
        });
    }

}
