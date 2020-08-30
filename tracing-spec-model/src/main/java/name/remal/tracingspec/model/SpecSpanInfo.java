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
import static lombok.AccessLevel.NONE;
import static name.remal.tracingspec.model.ComparatorUtils.compareNullLast;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.Data;
import lombok.Getter;
import lombok.val;
import org.jetbrains.annotations.Contract;

@NotThreadSafe
@Data
@JsonInclude(NON_EMPTY)
abstract class SpecSpanInfo<Self extends SpecSpanInfo<Self>> implements Comparable<Self> {

    @Nullable
    String parentSpanId;

    @Nullable
    String name;

    @Nullable
    SpecSpanKind kind;

    @Getter(NONE)
    @JsonInclude(NON_DEFAULT)
    boolean async;

    @Nullable
    String serviceName;

    @Nullable
    String remoteServiceName;

    @Nullable
    Instant startedAt;

    @Nullable
    String description;

    final Map<String, String> tags = new TagsMap(this);

    final List<SpecSpanAnnotation> annotations = new ArrayList<>();


    public boolean isAsync() {
        return async
            || (kind != null && kind.isAsync());
    }

    @JsonIgnore
    public boolean isSync() {
        return !isAsync();
    }


    public void setTags(Map<String, Object> tags) {
        val tagsMap = getTags();
        tagsMap.clear();
        tags.forEach((key, value) -> {
            if (key != null && value != null) {
                tagsMap.put(key, value.toString());
            }
        });
    }

    @Nullable
    public String getTag(String key) {
        return getTags().get(key);
    }

    @Contract("_, _ -> this")
    public Self putTag(String key, @Nullable String value) {
        if (value != null) {
            getTags().put(key, value);

        } else {
            getTags().remove(key);
        }

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }


    public void setAnnotations(Iterable<? extends SpecSpanAnnotation> annotations) {
        val annotationsList = getAnnotations();
        annotationsList.clear();
        annotations.forEach(annotationsList::add);
    }

    @Contract("_ -> this")
    public Self addAnnotation(SpecSpanAnnotation annotation) {
        val annotationsList = getAnnotations();
        annotationsList.add(annotation);

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }


    @Override
    public int compareTo(Self other) {
        return compareNullLast(getStartedAt(), other.getStartedAt());
    }

}
