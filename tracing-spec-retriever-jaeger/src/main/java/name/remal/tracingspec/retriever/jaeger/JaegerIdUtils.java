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

package name.remal.tracingspec.retriever.jaeger;

import static java.lang.System.arraycopy;
import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@SuppressWarnings("java:S109")
abstract class JaegerIdUtils {

    private static final int BYTES_LENGTH_DIVIDER = 8;
    private static final int CHARS_LENGTH_DIVIDER = BYTES_LENGTH_DIVIDER * 2;
    private static final String ZERO_CHARS;

    static {
        val sb = new StringBuilder(CHARS_LENGTH_DIVIDER);
        for (int i = 0; i < CHARS_LENGTH_DIVIDER; ++i) {
            sb.append('0');
        }
        ZERO_CHARS = sb.toString();
    }

    @SneakyThrows
    public static byte[] encodeJaegerId(String jaegerStringId) {
        byte[] bytes;
        if ((jaegerStringId.length() % 2) == 0) {
            bytes = decodeHex(jaegerStringId);
        } else {
            bytes = decodeHex('0' + jaegerStringId);
        }

        if ((bytes.length % BYTES_LENGTH_DIVIDER) != 0) {
            val newLength = ((bytes.length / BYTES_LENGTH_DIVIDER) + 1) * BYTES_LENGTH_DIVIDER;
            val newBytes = new byte[newLength];
            arraycopy(bytes, 0, newBytes, newLength - bytes.length, bytes.length);
            bytes = newBytes;
        }

        return bytes;
    }

    @SneakyThrows
    public static String decodeJaegerId(byte[] jaegerId) {
        String string = encodeHexString(jaegerId, true);

        if ((string.length() % CHARS_LENGTH_DIVIDER) != 0) {
            val newLength = ((string.length() / CHARS_LENGTH_DIVIDER) + 1) * CHARS_LENGTH_DIVIDER;
            string = ZERO_CHARS.substring(0, newLength - string.length()) + string;
        }

        return string;
    }


    private JaegerIdUtils() {
    }

}
