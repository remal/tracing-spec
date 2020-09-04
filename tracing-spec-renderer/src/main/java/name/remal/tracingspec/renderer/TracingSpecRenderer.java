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

package name.remal.tracingspec.renderer;

import java.util.List;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;

public interface TracingSpecRenderer<Result> {

    Result renderTracingSpec(List<SpecSpan> specSpans);

    void addNodeProcessor(SpecSpanNodeProcessor nodeProcessor);

    void addTagToDisplay(String tagName);

    default void addTagsToDisplay(String... tagNames) {
        for (val tagName : tagNames) {
            addTagToDisplay(tagName);
        }
    }

    default void addTagsToDisplay(Iterable<String> tagNames) {
        tagNames.forEach(this::addTagToDisplay);
    }

}
