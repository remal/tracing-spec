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

package utils.test.tracing;

import static java.util.Collections.newSetFromMap;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.val;

public abstract class SpanIdGenerator {

    private static final Random RANDOM = new SecureRandom();

    private static final Set<Integer> GENERATED_IDS = newSetFromMap(new ConcurrentHashMap<>());

    private static long nextLongSpanId() {
        while (true) {
            val id = RANDOM.nextInt();
            if (id <= 0) {
                continue;
            }
            if (GENERATED_IDS.add(id)) {
                return id;
            }
        }
    }

    public static String nextSpanId() {
        val idHex = Long.toHexString(nextLongSpanId());
        if ((idHex.length() % 8) == 0) {
            return idHex;
        }
        val resultLength = ((idHex.length() / 8) + 1) * 8;
        val result = "00000000".substring(0, resultLength - idHex.length()) + idHex;
        return result;
    }


    private SpanIdGenerator() {
    }

}
