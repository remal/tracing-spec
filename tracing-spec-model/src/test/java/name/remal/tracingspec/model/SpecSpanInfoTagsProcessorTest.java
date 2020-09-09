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

import static name.remal.tracingspec.model.SpecSpanKind.SERVER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class SpecSpanInfoTagsProcessorTest {

    private final SpecSpanInfo<?> info = mock(SpecSpanInfo.class);

    @Test
    void hidden_1() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.hidden", "1");
        verify(info).setHidden(true);
    }

    @Test
    void hidden_true() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.hidden", "tRuE");
        verify(info).setHidden(true);
    }

    @Test
    void kind() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.kind", "server");
        verify(info).setKind(SERVER);
    }

    @Test
    void async_1() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.async", "1");
        verify(info).setAsync(true);
    }

    @Test
    void async_true() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.async", "tRuE");
        verify(info).setAsync(true);
    }

    @Test
    void serviceName() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.serviceName", "service name");
        verify(info).setServiceName("service name");
    }

    @Test
    void remoteServiceName() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.remoteServiceName", "remote service name");
        verify(info).setRemoteServiceName("remote service name");
    }

    @Test
    void description() {
        SpecSpanInfoTagsProcessor.processTag(info, "spec.description", "description");
        verify(info).setDescription("description");
    }

}
