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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Contract;

@JsonInclude(NON_EMPTY)
interface SpecSpanInfo<Self extends SpecSpanInfo<Self>> {

    @Nullable
    String getName();

    void setName(@Nullable String name);


    @Nullable
    SpecSpanKind getKind();

    void setKind(@Nullable SpecSpanKind kind);


    @JsonInclude(NON_DEFAULT)
    boolean isAsync();

    void setAsync(boolean async);

    @JsonIgnore
    default boolean isSync() {
        return !isAsync();
    }


    @Nullable
    String getServiceName();

    void setServiceName(@Nullable String serviceName);


    @Nullable
    String getRemoteServiceName();

    void setRemoteServiceName(@Nullable String remoteServiceName);


    @Nullable
    Instant getStartedAt();

    void setStartedAt(@Nullable Instant startedAt);


    Map<String, String> getTags();

    @Contract("_, _ -> this")
    default Self putTag(String key, @Nullable String value) {
        if (value != null) {
            getTags().put(key, value);
        } else {
            getTags().remove(key);
        }

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }

    @Contract("_ -> this")
    default Self removeTag(String key) {
        getTags().remove(key);

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }


    Set<SpecSpanAnnotation> getAnnotations();

    @Contract("_, _ -> this")
    default Self addAnnotation(String key, String value) {
        getAnnotations().add(new SpecSpanAnnotation(key, value));

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }

    @Contract("_ -> this")
    default Self addAnnotation(String value) {
        getAnnotations().add(new SpecSpanAnnotation(value));

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }

}
