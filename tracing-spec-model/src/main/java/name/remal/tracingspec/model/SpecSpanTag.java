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

import java.util.Map;
import java.util.function.BiConsumer;
import lombok.val;
import name.remal.tracingspec.model.ImmutableSpecSpan.SpecSpanBuilder;

public enum SpecSpanTag {

    DESCRIPTION(SpecSpanBuilder::description),
    IS_ASYNC(booleanSetter(SpecSpanBuilder::async)),
    ;

    private final BiConsumer<SpecSpanBuilder, String> setter;

    SpecSpanTag(BiConsumer<SpecSpanBuilder, String> setter) {
        this.setter = setter;
    }


    private final String tagName = "spec." + this.name().toLowerCase().replace('_', '-');

    public String getTagName() {
        return tagName;
    }


    public void processTagsIntoBuilder(Map<String, ?> tagsMap, SpecSpanBuilder builder) {
        val valueObject = tagsMap.get(getTagName());
        if (valueObject != null) {
            val value = valueObject.toString();
            if (!value.isEmpty()) {
                setter.accept(builder, value);
            }
        }
    }

    public static void processAllTagsIntoBuilder(Map<String, ?> tagsMap, SpecSpanBuilder builder) {
        for (val tag : values()) {
            tag.processTagsIntoBuilder(tagsMap, builder);
        }
    }

    private static BiConsumer<SpecSpanBuilder, String> booleanSetter(BiConsumer<SpecSpanBuilder, Boolean> setter) {
        return (builder, value) -> {
            val booleanValue = "1".equals(value) || "true".equalsIgnoreCase(value);
            setter.accept(builder, booleanValue);
        };
    }

}
