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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import brave.Tracer;
import org.junit.jupiter.api.Test;

class SpecSpanTagsPointcutAdvisorTest extends AbstractAnnotationPointcutAdvisorTest {

    @Override
    protected AbstractAnnotationPointcutAdvisor<?> createAdvisor(
        Tracer tracer,
        TracingSpecSpringProperties properties
    ) {
        return new SpecSpanTagsPointcutAdvisor(tracer, properties);
    }

    @Test
    void getHiddenGetter() {
        assertThat(advisor.getHiddenGetter(), notNullValue());
    }

    @Test
    void getKindGetter() {
        assertThat(advisor.getKindGetter(), notNullValue());
    }

    @Test
    void getAsyncGetter() {
        assertThat(advisor.getAsyncGetter(), notNullValue());
    }

    @Test
    void getServiceNameGetter() {
        assertThat(advisor.getServiceNameGetter(), notNullValue());
    }

    @Test
    void getRemoteServiceNameGetter() {
        assertThat(advisor.getRemoteServiceNameGetter(), notNullValue());
    }

    @Test
    void getDescriptionGetter() {
        assertThat(advisor.getDescriptionGetter(), notNullValue());
    }

}
