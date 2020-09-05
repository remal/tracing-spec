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

package utils.test.resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.test.whocalled.WhoCalled.getCallerClass;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import lombok.SneakyThrows;
import lombok.val;
import org.intellij.lang.annotations.Language;

public abstract class Resources {

    public static URL getResourceUrl(@Language("file-reference") String resourceName) {
        val loaderClass = getCallerClass(1);
        return getResourceUrl(loaderClass, resourceName);
    }

    public static URL getResourceUrl(Class<?> loaderClass, @Language("file-reference") String resourceName) {
        URL resourceUrl = loaderClass.getResource(resourceName);
        if (resourceUrl == null) {
            resourceUrl = loaderClass.getResource('/' + resourceName);
        }
        if (resourceUrl == null) {
            throw new IllegalArgumentException(loaderClass + ": resource can't be found: " + resourceName);
        }
        return resourceUrl;
    }


    public static byte[] readBinaryResource(@Language("file-reference") String resourceName) {
        val loaderClass = getCallerClass(1);
        return readBinaryResource(loaderClass, resourceName);
    }

    @SneakyThrows
    public static byte[] readBinaryResource(Class<?> loaderClass, @Language("file-reference") String resourceName) {
        val resourceUrl = getResourceUrl(loaderClass, resourceName);
        try (val in = resourceUrl.openStream()) {
            try (val out = new ByteArrayOutputStream()) {
                val buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, read);
                }
                return out.toByteArray();
            }
        }
    }


    public static String readTextResource(@Language("file-reference") String resourceName) {
        val loaderClass = getCallerClass(1);
        return readTextResource(loaderClass, resourceName);
    }

    public static String readTextResource(Class<?> loaderClass, @Language("file-reference") String resourceName) {
        val bytesContent = readBinaryResource(loaderClass, resourceName);
        return new String(bytesContent, UTF_8);
    }


    private Resources() {
    }

}
