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
import javax.annotation.Nullable;
import lombok.val;

@SuppressWarnings({"java:S1157"})
abstract class SpecSpanInfoTagsProcessor {

    @SuppressWarnings({"java:S131", "checkstyle:MissingSwitchDefault"})
    public static void processTag(SpecSpanInfo<?> info, @Nullable String key, @Nullable String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            return;
        }

        switch (key) {
            case "spec.kind":
                if (info.getKind() == null) {
                    info.setKind(parseSpecSpanKind(value));
                }
                break;
            case "spec.async":
                if (!info.isAsync()) {
                    info.setAsync("1".equals(value) || "true".equals(value.toLowerCase()));
                }
                break;
            case "spec.serviceName":
                if (info.getServiceName() == null) {
                    info.setServiceName(value);
                }
                break;
            case "spec.remoteServiceName":
                if (info.getRemoteServiceName() == null) {
                    info.setRemoteServiceName(value);
                }
                break;
            case "spec.description":
                if (info.getDescription() == null) {
                    info.setDescription(value);
                }
                break;
        }
    }

    public static void processTags(SpecSpanInfo<?> info, Map<String, String> map) {
        for (val entry : map.entrySet()) {
            processTag(info, entry.getKey(), entry.getValue());
        }
    }


    private SpecSpanInfoTagsProcessor() {
    }

}
