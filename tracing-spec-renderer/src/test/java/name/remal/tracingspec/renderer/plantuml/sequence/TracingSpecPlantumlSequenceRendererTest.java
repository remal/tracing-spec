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

package name.remal.tracingspec.renderer.plantuml.sequence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import name.remal.tracingspec.renderer.plantuml.TracingSpecPlantumlRendererTestBase;

@SuppressWarnings("java:S2187")
class TracingSpecPlantumlSequenceRendererTest
    extends TracingSpecPlantumlRendererTestBase<TracingSpecPlantumlSequenceRenderer> {

    @Override
    protected void one_simple_span(String result) {
        assertThat(result, equalTo(readPlantumlDiagramResource("one-simple-span.puml")));
    }

    @Override
    protected void two_parents(String result) {
        assertThat(result, equalTo(readPlantumlDiagramResource("two-parents.puml")));
    }

    @Override
    protected void parent_child(String result) {
        assertThat(result, equalTo(readPlantumlDiagramResource("parent-child.puml")));
    }

    @Override
    protected void parent_children(String result) {
        assertThat(result, equalTo(readPlantumlDiagramResource("parent-children.puml")));
    }

    @Override
    protected void root_parent_child(String result) {
        assertThat(result, equalTo(readPlantumlDiagramResource("root-parent-child.puml")));
    }

    @Override
    protected void async_one_span(String result) {
        assertThat(result, equalTo(readPlantumlDiagramResource("async-one-span.puml")));
    }

    @Override
    protected void async_children(String result) {
        assertThat(result, equalTo(readPlantumlDiagramResource("async-children.puml")));
    }

}
