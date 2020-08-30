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

import static name.remal.tracingspec.model.SpecSpanInfoTagsProcessor.processTag;
import static name.remal.tracingspec.model.SpecSpanInfoTagsProcessor.processTags;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings({"java:S2160", "java:S1948"})
class TagsMap extends LinkedHashMap<String, String> {

    final SpecSpanInfo<?> info;

    @Override
    public void replaceAll(BiFunction<? super String, ? super String, ? extends String> function) {
        try {
            super.replaceAll(function);
        } finally {
            processTags(info, this);
        }
    }

    @Override
    @Nullable
    public String put(String key, String value) {
        try {
            return super.put(key, value);
        } finally {
            processTag(info, key, value);
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        try {
            super.putAll(m);
        } finally {
            processTags(info, this);
        }
    }

    @Override
    @Nullable
    public String putIfAbsent(String key, String value) {
        try {
            return super.putIfAbsent(key, value);
        } finally {
            processTags(info, this);
        }
    }

    @Override
    public boolean replace(String key, String oldValue, String newValue) {
        try {
            return super.replace(key, oldValue, newValue);
        } finally {
            processTags(info, this);
        }
    }

    @Override
    @Nullable
    public String replace(String key, String value) {
        try {
            return super.replace(key, value);
        } finally {
            processTag(info, key, value);
        }
    }

    @Override
    @Nullable
    public String computeIfAbsent(String key, Function<? super String, ? extends String> mappingFunction) {
        try {
            return super.computeIfAbsent(key, mappingFunction);
        } finally {
            processTags(info, this);
        }
    }

    @Override
    @Nullable
    public String computeIfPresent(
        String key,
        BiFunction<? super String, ? super String, ? extends String> remappingFunction
    ) {
        try {
            return super.computeIfPresent(key, remappingFunction);
        } finally {
            processTags(info, this);
        }
    }

    @Override
    @Nullable
    public String compute(String key, BiFunction<? super String, ? super String, ? extends String> remappingFunction) {
        try {
            return super.compute(key, remappingFunction);
        } finally {
            processTags(info, this);
        }
    }

    @Override
    @Nullable
    public String merge(
        String key,
        String value,
        BiFunction<? super String, ? super String, ? extends String> remappingFunction
    ) {
        try {
            return super.merge(key, value, remappingFunction);
        } finally {
            processTags(info, this);
        }
    }

}
