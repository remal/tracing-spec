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

import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

import lombok.SneakyThrows;
import lombok.var;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
@SuppressWarnings("java:S109")
interface JaegerIdUtils {

    @SneakyThrows
    static byte[] encodeJaegerId(String jaegerStringId) {
        if ((jaegerStringId.length() % 2) == 0) {
            return decodeHex(jaegerStringId);
        } else {
            return decodeHex('0' + jaegerStringId);
        }
    }

    @SneakyThrows
    static String decodeJaegerTraceId(byte[] jaegerId) {
        var string = encodeHexString(jaegerId, true);
        if (string.length() < 16) {
            string = "0000000000000000".substring(string.length()) + string;
        }
        while (string.length() > 16 && string.charAt(0) == '0') {
            string = string.substring(1);
        }
        return string;
    }

    @SneakyThrows
    static String decodeJaegerSpanId(byte[] jaegerId) {
        var string = encodeHexString(jaegerId, true);
        while (string.length() >= 2 && string.charAt(0) == '0') {
            string = string.substring(1);
        }
        return string;
    }

}
