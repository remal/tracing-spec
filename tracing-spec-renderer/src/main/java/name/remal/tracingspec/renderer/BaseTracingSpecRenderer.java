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

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;

public abstract class BaseTracingSpecRenderer<Result> implements TracingSpecRenderer<Result> {

    @Nullable
    private ServiceNameTransformer serviceNameTransformer;

    @Override
    public final void setServiceNameTransformer(@Nullable ServiceNameTransformer serviceNameTransformer) {
        this.serviceNameTransformer = serviceNameTransformer;
    }


    @Nullable
    private SpecSpansFilter specSpansFilter;

    @Override
    public final void setSpecSpansFilter(@Nullable SpecSpansFilter specSpansFilter) {
        this.specSpansFilter = specSpansFilter;
    }


    @Override
    public final Result renderTracingSpec(List<SpecSpan> specSpans) {
        if (specSpans.isEmpty()) {
            throw new IllegalArgumentException("specSpans must not be empty");
        }


        specSpans = transformServiceNames(specSpans);


        specSpans = filterSpecSpans(specSpans);
        if (specSpans.isEmpty()) {
            throw new IllegalStateException("All specSpans were filtered out");
        }


        boolean haveSpansWithServiceName = false;
        boolean haveSpansWithoutServiceName = false;
        for (val span : specSpans) {
            if (span.getServiceName().isPresent()) {
                haveSpansWithServiceName = true;
            } else {
                haveSpansWithoutServiceName = true;
            }
        }
        if (haveSpansWithServiceName && haveSpansWithoutServiceName) {
            throw new IllegalStateException("Some filtered specSpans have serviceName and some - not");
        }


        return renderFilteredTracingSpec(specSpans);
    }

    @SneakyThrows
    private List<SpecSpan> transformServiceNames(List<SpecSpan> specSpans) {
        val currentServiceNameTransformer = this.serviceNameTransformer;
        if (currentServiceNameTransformer == null) {
            return specSpans;
        }

        List<SpecSpan> result = new ArrayList<>();
        for (val specSpan : specSpans) {
            val serviceName = specSpan.getServiceName();
            if (serviceName.isPresent()) {
                val transformedServiceName = currentServiceNameTransformer.transform(serviceName.get());
                val transformedSpecSpan = SpecSpan.builder()
                    .from(specSpan)
                    .serviceName(transformedServiceName)
                    .build();
                result.add(transformedSpecSpan);

            } else {
                result.add(specSpan);
            }
        }

        return result;
    }

    @SneakyThrows
    private List<SpecSpan> filterSpecSpans(List<SpecSpan> specSpans) {
        val currentSpecSpansFilter = this.specSpansFilter;
        if (currentSpecSpansFilter == null) {
            return specSpans;
        }

        List<SpecSpan> result = new ArrayList<>();
        for (val specSpan : specSpans) {
            if (currentSpecSpansFilter.test(specSpan)) {
                result.add(specSpan);
            }
        }

        return result;
    }

    protected abstract Result renderFilteredTracingSpec(List<SpecSpan> specSpans);


    protected static int compareByStartedAt(SpecSpan span1, SpecSpan span2) {
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

}
