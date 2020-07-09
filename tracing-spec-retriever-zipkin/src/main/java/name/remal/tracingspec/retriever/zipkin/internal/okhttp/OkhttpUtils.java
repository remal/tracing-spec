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

package name.remal.tracingspec.retriever.zipkin.internal.okhttp;

import static java.lang.Character.isISOControl;
import static java.lang.Character.isWhitespace;
import static java.lang.Math.min;

import java.io.EOFException;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.api.ExcludeFromCodeCoverage;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@ExcludeFromCodeCoverage
public interface OkhttpUtils {

    @SuppressWarnings("java:S109")
    static boolean isPlaintext(Buffer buffer) {
        try {
            val prefix = new Buffer();
            long byteCount = min(buffer.size(), 64);
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (isISOControl(codePoint) && !isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence
        }
    }

    @SneakyThrows
    static boolean isPlaintext(ResponseBody responseBody) {
        val source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body
        val buffer = source.getBuffer();
        return isPlaintext(buffer);
    }

}
