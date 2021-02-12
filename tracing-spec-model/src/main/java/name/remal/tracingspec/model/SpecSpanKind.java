package name.remal.tracingspec.model;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING;

import com.fasterxml.jackson.annotation.JsonCreator;
import javax.annotation.Nullable;
import lombok.val;
import org.jetbrains.annotations.Contract;

public enum SpecSpanKind {

    CLIENT,
    SERVER,
    PRODUCER,
    CONSUMER(true, true),
    ;

    private final boolean alwaysAsync;
    private final boolean remoteSource;

    SpecSpanKind(boolean alwaysAsync, boolean remoteSource) {
        this.alwaysAsync = alwaysAsync;
        this.remoteSource = remoteSource;
    }

    SpecSpanKind() {
        this(false, false);
    }

    public boolean isAsync() {
        return alwaysAsync;
    }

    public boolean isRemoteSource() {
        return remoteSource;
    }


    @Nullable
    @Contract("null -> null")
    @JsonCreator(mode = DELEGATING)
    public static SpecSpanKind parseSpecSpanKind(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        for (val kind : values()) {
            if (kind.name().equalsIgnoreCase(value)) {
                return kind;
            }
        }
        return null;
    }

}
