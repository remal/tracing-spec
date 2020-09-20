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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.Data;
import lombok.val;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;

@Internal
@NotThreadSafe
@Data
@JsonInclude(NON_EMPTY)
public abstract class SpecSpanInfo<Self extends SpecSpanInfo<Self>> implements Comparable<Self> {

    @JsonInclude(NON_DEFAULT)
    @JsonProperty(index = 101)
    boolean hidden;

    @Nullable
    @JsonProperty(index = 102)
    String name;

    @Nullable
    @JsonProperty(index = 103)
    SpecSpanKind kind;

    @JsonInclude(NON_DEFAULT)
    @JsonProperty(index = 104)
    boolean async;

    @Nullable
    @JsonProperty(index = 105)
    String serviceName;

    @Nullable
    @JsonProperty(index = 106)
    String remoteServiceName;

    @Nullable
    @JsonProperty(index = 107)
    Instant startedAt;

    @Nullable
    @JsonProperty(index = 108)
    String description;

    @JsonProperty(index = 109)
    final Map<String, String> tags = new TagsMap(this);

    @JsonProperty(index = 110)
    final List<SpecSpanAnnotation> annotations = new ArrayList<>();


    public void setName(@Nullable String name) {
        this.name = name == null || name.isEmpty() ? null : name;
    }

    public void setServiceName(@Nullable String serviceName) {
        this.serviceName = serviceName == null || serviceName.isEmpty() ? null : serviceName;
    }

    public void setRemoteServiceName(@Nullable String remoteServiceName) {
        this.remoteServiceName = remoteServiceName == null || remoteServiceName.isEmpty() ? null : remoteServiceName;
    }

    public void setDescription(@Nullable String description) {
        this.description = description == null || description.isEmpty() ? null : description;
    }


    public boolean isAsync() {
        if (async) {
            return true;
        }
        val currentKind = getKind();
        return currentKind != null && currentKind.isAsync();
    }

    @JsonIgnore
    public final boolean isSync() {
        return !isAsync();
    }


    public void setTags(Map<String, Object> tags) {
        this.tags.clear();
        tags.forEach((key, value) -> {
            if (key != null && value != null) {
                this.tags.put(key, value.toString());
            }
        });
    }

    @Nullable
    public String getTag(String key) {
        return tags.get(key);
    }

    @Contract("_, _ -> this")
    public Self putTag(String key, @Nullable Object value) {
        if (value != null) {
            tags.put(key, value.toString());

        } else {
            tags.remove(key);
        }

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }


    public void setAnnotations(Iterable<? extends SpecSpanAnnotation> annotations) {
        this.annotations.clear();
        annotations.forEach(this.annotations::add);
    }

    @Contract("_ -> this")
    public Self addAnnotation(SpecSpanAnnotation annotation) {
        this.annotations.add(annotation);

        @SuppressWarnings("unchecked")
        val self = (Self) this;
        return self;
    }


    @Override
    public int compareTo(Self other) {
        return compareNullLast(getStartedAt(), other.getStartedAt());
    }

}
