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

package name.remal.tracingspec.model.internal;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.USE_DEFAULTS;
import static com.fasterxml.jackson.core.util.VersionUtil.parseVersion;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getStringProperty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder.Value;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.val;
import name.remal.gradle_plugins.api.AutoService;

@AutoService(Module.class)
public class JacksonTracingSpecBuilderSettingsModule extends Module {

    private static final String MODULE_NAME = JacksonTracingSpecBuilderSettingsModule.class.getName();
    private static final Version MODULE_VERSION = parseVersion(
        getStringProperty("version"),
        getStringProperty("group"),
        getStringProperty("name")
    );

    @Override
    public String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    public Version version() {
        return MODULE_VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.insertAnnotationIntrospector(new TracingSpecBuilderSettingsAnnotationIntrospector());
    }


    private static class TracingSpecBuilderSettingsAnnotationIntrospector extends AnnotationIntrospector {
        @Override
        public Version version() {
            return MODULE_VERSION;
        }

        @Override
        @Nullable
        public JsonInclude.Value findPropertyInclusion(Annotated annotated) {
            if (annotated instanceof AnnotatedClass) {
                val annotatedClass = (AnnotatedClass) annotated;
                if (annotatedClass.getName().startsWith("name.remal.tracingspec.")) {
                    return JsonInclude.Value.construct(NON_EMPTY, USE_DEFAULTS);
                }
            }
            return super.findPropertyInclusion(annotated);
        }

        @Override
        @Nullable
        @SuppressWarnings({"java:S134", "java:S3776"})
        public Class<?> findPOJOBuilder(AnnotatedClass annotatedClass) {
            if (annotatedClass.getName().startsWith("name.remal.tracingspec.")) {
                val clazz = annotatedClass.getRawType();
                val className = clazz.getName();

                Map<String, Class<?>> candidateBuilderClassNames = new LinkedHashMap<>();

                // Lombok:
                candidateBuilderClassNames.put(className + '$' + clazz.getSimpleName() + "Builder", clazz);

                // Immutables:
                for (val interfaceClass : clazz.getInterfaces()) {
                    candidateBuilderClassNames.put(
                        className + '$' + interfaceClass.getSimpleName() + "Builder",
                        clazz
                    );
                }
                {
                    final String targetPrefix;
                    int packageEndPos = className.lastIndexOf('.');
                    if (packageEndPos > 0) {
                        targetPrefix = className.substring(0, packageEndPos + 1);
                    } else {
                        targetPrefix = "";
                    }
                    for (val generatedPrefix : new String[]{"Immutable", "Modifiable"}) {
                        val targetClassName = targetPrefix + generatedPrefix + clazz.getSimpleName();
                        final Class<?> targetClass;
                        try {
                            targetClass = Class.forName(targetClassName, false, clazz.getClassLoader());
                        } catch (Throwable ignored) {
                            continue;
                        }
                        candidateBuilderClassNames.put(
                            targetClassName + '$' + clazz.getSimpleName() + "Builder",
                            targetClass
                        );
                    }
                }

                for (val candidateBuilderClassNameEntry : candidateBuilderClassNames.entrySet()) {
                    val candidateBuilderClassName = candidateBuilderClassNameEntry.getKey();
                    val targetClass = candidateBuilderClassNameEntry.getValue();
                    try {
                        val builderClass = Class.forName(candidateBuilderClassName, false, clazz.getClassLoader());
                        val builderMethod = builderClass.getMethod("build");
                        if (builderMethod.getReturnType() == targetClass) {
                            return builderClass;
                        }
                    } catch (Throwable ignored) {
                        // do nothing
                    }
                }
            }
            return super.findPOJOBuilder(annotatedClass);
        }

        @Override
        @Nullable
        public Value findPOJOBuilderConfig(AnnotatedClass annotatedClass) {
            if (annotatedClass.getName().startsWith("name.remal.tracingspec.")) {
                return new JsonPOJOBuilder.Value("build", "");
            }
            return super.findPOJOBuilderConfig(annotatedClass);
        }
    }

}
