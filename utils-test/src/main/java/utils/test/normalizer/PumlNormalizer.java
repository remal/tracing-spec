package utils.test.normalizer;

import static java.util.stream.Collectors.joining;

import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.val;

public abstract class PumlNormalizer {

    private static final Pattern NEW_LINES = Pattern.compile("[\n\r]+");

    public static String normalizePuml(String diagram) {
        val lines = NEW_LINES.split(diagram);
        return Stream.of(lines)
            .map(String::trim)
            .filter(line -> !line.isEmpty())
            .collect(joining("\n"));
    }


    private PumlNormalizer() {
    }

}
