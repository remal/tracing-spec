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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;

public abstract class SpecSpansSorter {

    public static List<SpecSpan> toSortedSpecSpans(List<SpecSpan> specSpans) {
        List<SpecSpan> result = new ArrayList<>(specSpans.size());
        collectSortedSpecSpans(specSpans, null, result);
        return result;
    }

    private static void collectSortedSpecSpans(
        List<SpecSpan> specSpans,
        @Nullable String parentSpanId,
        List<SpecSpan> target
    ) {
        val currentSiblings = specSpans.stream()
            .filter(span -> Objects.equals(
                span.getParentSpanId().orElse(null),
                parentSpanId
            ))
            .sorted(SpecSpansSorter::compareByStartedAt)
            .collect(toList());
        for (val span : currentSiblings) {
            target.add(span);
            collectSortedSpecSpans(
                specSpans,
                span.getSpanId(),
                target
            );
        }
    }

    private static int compareByStartedAt(SpecSpan span1, SpecSpan span2) {
        val startedAt1 = span1.getStartedAt();
        val startedAt2 = span2.getStartedAt();
        if (startedAt1.isPresent() && startedAt2.isPresent()) {
            return startedAt1.get().compareTo(startedAt2.get());

        } else if (startedAt1.isPresent()) {
            return -1;

        } else if (startedAt2.isPresent()) {
            return 1;

        } else {
            return 0;
        }
    }


    private SpecSpansSorter() {
    }

}
