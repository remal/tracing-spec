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

package name.remal.tracingspec.spring;

import brave.Tracer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

@Internal
class SpecSpanTagsPointcutAdvisor extends AbstractAnnotationPointcutAdvisor<SpecSpanTags> {

    public SpecSpanTagsPointcutAdvisor(Tracer tracer, TracingSpecSpringProperties properties) {
        super(tracer, properties);
    }

    @Nullable
    @Override
    protected Predicate<SpecSpanTags> getHiddenGetter() {
        return SpecSpanTags::hidden;
    }

    @Nullable
    @Override
    protected Function<SpecSpanTags, String> getKindGetter() {
        return SpecSpanTags::kind;
    }

    @Nullable
    @Override
    protected Predicate<SpecSpanTags> getAsyncGetter() {
        return SpecSpanTags::async;
    }

    @Nullable
    @Override
    protected Function<SpecSpanTags, String> getServiceNameGetter() {
        return SpecSpanTags::serviceName;
    }

    @Nullable
    @Override
    protected Function<SpecSpanTags, String> getRemoteServiceNameGetter() {
        return SpecSpanTags::remoteServiceName;
    }

    @Override
    protected Function<SpecSpanTags, String> getDescriptionGetter() {
        return SpecSpanTags::description;
    }

}
