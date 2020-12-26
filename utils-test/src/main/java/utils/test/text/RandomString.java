package utils.test.text;

import static java.lang.Character.isLetterOrDigit;
import static java.util.Collections.newSetFromMap;

import java.security.SecureRandom;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.val;

public abstract class RandomString {

    private static final Random RANDOM = new SecureRandom();

    private static final Set<String> GENERATED_STRING = newSetFromMap(new ConcurrentHashMap<>());

    public static String nextRandomString() {
        return nextRandomString(20);
    }

    @SuppressWarnings("java:S881")
    public static String nextRandomString(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length ts less than 0");
        }

        int maxAttempts = 1_000_000;
        int attempt = 0;
        while ((++attempt) <= maxAttempts) {
            val sb = new StringBuilder(length);
            for (int n = 1; n <= length; ++n) {
                char ch = (char) RANDOM.nextInt(127);
                if (isLetterOrDigit(ch)) {
                    sb.append(ch);
                }
            }
            val string = sb.toString();
            if (GENERATED_STRING.add(string)) {
                return string;
            }
        }
        throw new RuntimeException("All " + maxAttempts + " attempts failed");
    }


    private RandomString() {
    }

}
