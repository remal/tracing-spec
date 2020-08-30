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

import static name.remal.tracingspec.model.SpecSpansGraphs.createSpecSpansGraph;

import java.util.List;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.model.SpecSpansGraph;
import org.jetbrains.annotations.Contract;

public abstract class BaseTracingSpecRenderer<Result> implements TracingSpecRenderer<Result> {

    protected abstract Result renderSpecSpansGraph(SpecSpansGraph specSpansGraph);

    @Override
    public final Result renderTracingSpec(List<SpecSpan> specSpans) {
        if (specSpans.isEmpty()) {
            throw new IllegalArgumentException("specSpans must not be empty");
        }


        boolean haveSpansWithServiceName = false;
        boolean haveSpansWithoutServiceName = false;
        for (val span : specSpans) {
            if (span.getServiceName() != null) {
                haveSpansWithServiceName = true;
            } else {
                haveSpansWithoutServiceName = true;
            }
        }
        if (haveSpansWithServiceName && haveSpansWithoutServiceName) {
            throw new IllegalStateException("Some specSpans do have serviceName and some - not");
        }


        val specSpansGraph = createSpecSpansGraph(specSpans);
        return renderSpecSpansGraph(specSpansGraph);
    }


    @Contract("null -> true")
    protected static boolean isEmpty(@Nullable CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    @Contract("null -> false")
    protected static boolean isNotEmpty(@Nullable CharSequence charSequence) {
        return !isEmpty(charSequence);
    }

}
