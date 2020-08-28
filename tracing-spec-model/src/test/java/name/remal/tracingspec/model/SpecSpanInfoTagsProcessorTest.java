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
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SpecSpanInfoTagsProcessorTest {

    private final SpecSpanInfo<?> info = mock(SpecSpanInfo.class);

    private final Map<String, String> tags = new HashMap<>();

    {
        when(info.getTags()).thenReturn(tags);
    }


    @Nested
    class Kind {

        @Test
        void kind() {
            tags.put("spec.kind", "server");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setKind(SERVER);
        }

    }


    @Nested
    class Async {

        @Test
        void async_1() {
            tags.put("spec.async", "1");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setAsync(true);
        }

        @Test
        void async_true() {
            tags.put("spec.async", "tRuE");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setAsync(true);
        }

        @Test
        void isAsync_1() {
            tags.put("spec.isAsync", "1");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setAsync(true);
        }

        @Test
        void isAsync_true() {
            tags.put("spec.isAsync", "tRuE");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setAsync(true);
        }

        @Test
        void is_async_1() {
            tags.put("spec.is-async", "1");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setAsync(true);
        }

        @Test
        void is_async_true() {
            tags.put("spec.is-async", "tRuE");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setAsync(true);
        }

    }


    @Nested
    class ServiceName {

        @Test
        void serviceName() {
            tags.put("spec.serviceName", "service name");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setServiceName("service name");
        }

        @Test
        void service_name() {
            tags.put("spec.service-name", "service name");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setServiceName("service name");
        }

    }


    @Nested
    class RemoteServiceName {

        @Test
        void serviceName() {
            tags.put("spec.remoteServiceName", "remote service name");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setRemoteServiceName("remote service name");
        }

        @Test
        void remote_service_name() {
            tags.put("spec.remote-service-name", "remote service name");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setRemoteServiceName("remote service name");
        }

    }


    @Nested
    class Description {

        @Test
        void description() {
            tags.put("spec.description", "description");
            SpecSpanInfoTagsProcessor.processTags(info);
            verify(info).setDescription("description");
        }

    }

}
