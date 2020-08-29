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

import static name.remal.tracingspec.model.SpecSpanKind.parseSpecSpanKind;

import java.util.Map;
import java.util.function.Consumer;
import lombok.val;

@SuppressWarnings({"java:S1157"})
abstract class SpecSpanInfoTagsProcessor {

    public static void processTags(SpecSpanInfo<?> info) {
        val tags = info.getTags();

        if (info.getKind() == null) {
            processTagValue(
                value -> info.setKind(parseSpecSpanKind(value)),
                tags,
                "spec.kind"
            );
        }

        if (!info.isAsync()) {
            processTagValue(
                value -> info.setAsync("1".equals(value) || "true".equals(value.toLowerCase())),
                tags,
                "spec.async",
                "spec.isAsync",
                "spec.is-async"
            );
        }

        if (info.getServiceName() == null) {
            processTagValue(
                info::setServiceName,
                tags,
                "spec.serviceName",
                "spec.service-name"
            );
        }

        if (info.getRemoteServiceName() == null) {
            processTagValue(
                info::setRemoteServiceName,
                tags,
                "spec.remoteServiceName",
                "spec.remote-service-name"
            );
        }

        if (info.getDescription() == null) {
            processTagValue(
                info::setDescription,
                tags,
                "spec.description"
            );
        }
    }

    private static void processTagValue(
        Consumer<String> consumer,
        Map<String, String> tags,
        String key,
        String... alternativeKeys
    ) {
        String value = tags.get(key);
        if (value != null) {
            consumer.accept(value);
            return;
        }
        for (val alternativeKey : alternativeKeys) {
            value = tags.get(alternativeKey);
            if (value != null) {
                consumer.accept(value);
                return;
            }
        }
    }


    private SpecSpanInfoTagsProcessor() {
    }

}
