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
import static name.remal.tracingspec.model.ComparatorUtils.compareNullLast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Contract;

@JsonInclude(NON_EMPTY)
interface SpecSpanInfo<Self extends SpecSpanInfo<Self>> extends Comparable<Self> {

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


    @Nullable
    String getDescription();

    void setDescription(@Nullable String description);


    Map<String, String> getTags();

    default void setTags(Map<String, Object> tags) {
        val tagsMap = getTags();
        tagsMap.clear();
        tags.forEach((key, value) -> {
            if (key != null && value != null) {
                tagsMap.put(key, value.toString());
            }
        });
    }

    @Nullable
    default String getTag(String key) {
        return getTags().get(key);
    }

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


    List<SpecSpanAnnotation> getAnnotations();

    default void setAnnotations(Iterable<? extends SpecSpanAnnotation> annotations) {
        val annotationsList = getAnnotations();
        annotationsList.clear();
        annotations.forEach(annotationsList::add);
    }

    @Contract("_ -> this")
    default Self addAnnotation(SpecSpanAnnotation annotation) {
        val annotationsList = getAnnotations();
        annotationsList.add(annotation);

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }


    @Override
    default int compareTo(Self other) {
        return compareNullLast(getStartedAt(), other.getStartedAt());
    }

}
