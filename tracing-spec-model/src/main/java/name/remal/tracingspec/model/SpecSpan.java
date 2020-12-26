package name.remal.tracingspec.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.Data;

@NotThreadSafe
@Data
public class SpecSpan extends SpecSpanInfo<SpecSpan> {

    @JsonProperty(index = 1)
    final String spanId;

    @Nullable
    @JsonProperty(index = 2)
    String parentSpanId;


    public SpecSpan(@JsonProperty("spanId") String spanId) {
        if (spanId.isEmpty()) {
            throw new IllegalArgumentException("spanId must not be empty");
        }
        this.spanId = spanId;
    }


    public void setParentSpanId(@Nullable String parentSpanId) {
        this.parentSpanId = parentSpanId == null || parentSpanId.isEmpty() ? null : parentSpanId;
    }

}
