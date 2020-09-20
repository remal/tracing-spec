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

package name.remal.tracingspec.application;

import static name.remal.gradle_plugins.api.BuildTimeConstants.getStringProperty;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.springframework.stereotype.Component;
import picocli.CommandLine.IVersionProvider;

@Internal
@Component
class VersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[]{
            getStringProperty("version")
        };
    }

}
