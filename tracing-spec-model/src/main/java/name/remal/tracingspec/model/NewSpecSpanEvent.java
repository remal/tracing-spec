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

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(doNotUseGetters = true)
@EqualsAndHashCode(doNotUseGetters = true)
public class NewSpecSpanEvent {

    @Nullable
    final String key;

    final String value;

    public NewSpecSpanEvent(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public NewSpecSpanEvent(String value) {
        this.key = null;
        this.value = value;
    }


    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

}
