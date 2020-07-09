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
import lombok.var;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@SuppressWarnings("java:S109")
interface JaegerIdUtils {

    @SneakyThrows
    static byte[] encodeJaegerId(String jaegerStringId) {
        byte[] bytes;
        if ((jaegerStringId.length() % 2) == 0) {
            bytes = decodeHex(jaegerStringId);
        } else {
            bytes = decodeHex('0' + jaegerStringId);
        }

        if ((bytes.length % 8) != 0) {
            val newLength = ((bytes.length / 8) + 1) * 8;
            val newBytes = new byte[newLength];
            arraycopy(bytes, 0, newBytes, newLength - bytes.length, bytes.length);
            bytes = newBytes;
        }

        return bytes;
    }

    @SneakyThrows
    static String decodeJaegerId(byte[] jaegerId) {
        var string = encodeHexString(jaegerId, true);

        int notZeroCharIndex = 0;
        while (notZeroCharIndex < string.length() - 1 && string.charAt(notZeroCharIndex) == '0') {
            ++notZeroCharIndex;
        }
        if (notZeroCharIndex > 0) {
            string = string.substring(notZeroCharIndex);
        }

        return string;
    }

}