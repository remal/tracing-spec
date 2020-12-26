package utils.test.tracing;

import static java.util.Collections.newSetFromMap;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.val;

public abstract class SpanIdGenerator {

    private static final Random RANDOM = new SecureRandom();

    private static final Set<Long> GENERATED_IDS = newSetFromMap(new ConcurrentHashMap<>());

    @SuppressWarnings("java:S881")
    private static long nextLongSpanId() {
        int maxAttempts = 1_000_000;
        int attempt = 0;
        while ((++attempt) <= maxAttempts) {
            final long id;
            if (RANDOM.nextBoolean()) {
                id = RANDOM.nextInt();
            } else {
                id = ((long) Integer.MAX_VALUE) + RANDOM.nextInt();
            }
            if (id <= 0) {
                continue;
            }
            if (GENERATED_IDS.add(id)) {
                return id;
            }
        }
        throw new RuntimeException("All " + maxAttempts + " attempts failed");
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
