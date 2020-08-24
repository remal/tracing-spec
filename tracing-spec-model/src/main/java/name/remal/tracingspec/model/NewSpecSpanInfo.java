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
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

interface NewSpecSpanInfo {

    @Nullable
    String getName();

    void setName(@Nullable String name);


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

    default void putTag(String key, @Nullable String value) {
        if (value != null) {
            getTags().put(key, value);
        } else {
            getTags().remove(key);
        }
    }

    default void removeTag(String key) {
        getTags().remove(key);
    }


    List<NewSpecSpanEvent> getEvents();

    default void addEvent(NewSpecSpanEvent event) {
        getEvents().add(event);
    }

    default void addEvent(String key, String value) {
        getEvents().add(new NewSpecSpanEvent(key, value));
    }

    default void addEvent(String value) {
        getEvents().add(new NewSpecSpanEvent(value));
    }

}
