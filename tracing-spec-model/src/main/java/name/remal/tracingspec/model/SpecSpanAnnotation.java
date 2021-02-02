package name.remal.tracingspec.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static name.remal.tracingspec.model.ComparatorUtils.compareNullLast;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import javax.annotation.Nullable;
import lombok.Data;

@Data
@JsonInclude(NON_EMPTY)
public class SpecSpanAnnotation implements Comparable<SpecSpanAnnotation> {

    @Nullable
    @JsonProperty(index = 1)
    final Instant instant;

    @Nullable
    @JsonProperty(index = 2)
    final String key;

    @JsonProperty(index = 3)
    final String value;

    public SpecSpanAnnotation(Instant instant, String key, String value) {
        this.instant = instant;
        this.key = key;
        this.value = value;
    }

    public SpecSpanAnnotation(Instant instant, String value) {
        this.instant = instant;
        this.key = null;
        this.value = value;
    }

    public SpecSpanAnnotation(String key, String value) {
        this.instant = null;
        this.key = key;
        this.value = value;
    }

    public SpecSpanAnnotation(String value) {
        this.instant = null;
        this.key = null;
        this.value = value;
    }


    @Override
    public int compareTo(SpecSpanAnnotation other) {
        return compareNullLast(getInstant(), other.getInstant());
    }

}
