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

package name.remal.tracingspec.renderer.plantuml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static utils.test.normalizer.PumlNormalizer.normalizePuml;
import static utils.test.resource.Resources.readTextResource;

import lombok.val;
import name.remal.tracingspec.renderer.TracingSpecRenderer;
import name.remal.tracingspec.renderer.TracingSpecRendererTestBase;
import org.intellij.lang.annotations.Language;

public abstract class TracingSpecPlantumlRendererTestBase<Renderer extends TracingSpecRenderer<String>>
    extends TracingSpecRendererTestBase<String, Renderer> {

    @Override
    protected String normalizeResult(String diagram) {
        return normalizePuml(diagram);
    }

    @Override
    protected String getExpectedResourceName(String resourceName) {
        val classResourcePrefix = getClass().getName()
            .substring(0, getClass().getName().lastIndexOf('.') + 1)
            .replace('.', '/');

        val resourceNamePrefix = resourceName.substring(0, resourceName.lastIndexOf('/') + 1);
        assertThat(classResourcePrefix, startsWith(resourceNamePrefix));

        val relativePrefix = classResourcePrefix.substring(resourceNamePrefix.length());

        final String expectedResourceFileName;
        {
            val resourceFileName = resourceName.substring(resourceName.lastIndexOf('/') + 1);
            val resourceBaseFileName = resourceFileName.substring(0, resourceFileName.lastIndexOf('.'));
            expectedResourceFileName = resourceBaseFileName + ".puml";
        }

        return resourceNamePrefix + relativePrefix + expectedResourceFileName;
    }

    @Override
    protected String loadExpectedResult(@Language("file-reference") String expectedResourceName) {
        return readTextResource(expectedResourceName);
    }

}
