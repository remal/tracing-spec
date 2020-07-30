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

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;
import lombok.val;

public abstract class SpanIdGenerator {

    private static final AtomicLong SPAN_ID;

    static {
        val random = new SecureRandom();
        long initialValue;
        do {
            initialValue = random.nextInt();
        } while (initialValue < 0);

        SPAN_ID = new AtomicLong(initialValue);
    }

    private static long nextLongSpanId() {
        long id = SPAN_ID.incrementAndGet();
        while (id < 0) {
            if (SPAN_ID.compareAndSet(id, 0)) {
                return 0;
            }
            id = SPAN_ID.incrementAndGet();
        }
        return id;
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