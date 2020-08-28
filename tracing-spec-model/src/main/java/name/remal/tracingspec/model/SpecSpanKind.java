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

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nullable;
import lombok.val;

public enum SpecSpanKind {

    CLIENT,
    SERVER,
    PRODUCER,
    CONSUMER,
    ;

    @Nullable
    @JsonCreator(mode = DELEGATING)
    public static SpecSpanKind parseSpecSpanKind(String value) {
        for (val kind : values()) {
            if (kind.name().equalsIgnoreCase(value)) {
                return kind;
            }
        }
        return null;
    }

}
