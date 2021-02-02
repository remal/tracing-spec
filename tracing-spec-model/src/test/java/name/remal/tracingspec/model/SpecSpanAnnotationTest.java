package name.remal.tracingspec.model;

import static java.time.Instant.ofEpochSecond;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

class SpecSpanAnnotationTest {

    @Test
    void compareTo() {
        assertThat(new SpecSpanAnnotation("").compareTo(new SpecSpanAnnotation("")), equalTo(0));
        assertThat(new SpecSpanAnnotation(ofEpochSecond(1), "").compareTo(new SpecSpanAnnotation("")), equalTo(-1));
        assertThat(new SpecSpanAnnotation("").compareTo(new SpecSpanAnnotation(ofEpochSecond(1), "")), equalTo(1));

        assertThat(
            new SpecSpanAnnotation(ofEpochSecond(1), "").compareTo(new SpecSpanAnnotation(ofEpochSecond(1), "")),
            equalTo(0)
        );
        assertThat(
            new SpecSpanAnnotation(ofEpochSecond(2), "").compareTo(new SpecSpanAnnotation(ofEpochSecond(1), "")),
            equalTo(1)
        );
        assertThat(
            new SpecSpanAnnotation(ofEpochSecond(1), "").compareTo(new SpecSpanAnnotation(ofEpochSecond(2), "")),
            equalTo(-1)
        );
    }

}
