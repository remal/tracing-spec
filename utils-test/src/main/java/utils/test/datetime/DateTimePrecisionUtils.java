package utils.test.datetime;

import static java.lang.Math.toIntExact;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import lombok.val;

public abstract class DateTimePrecisionUtils {

    public static Duration withMicrosecondsPrecision(Duration duration) {
        return Duration.ofSeconds(
            duration.getSeconds(),
            withMicrosecondsPrecision(duration.getNano())
        );
    }

    public static Instant withMicrosecondsPrecision(Instant instant) {
        return Instant.ofEpochSecond(
            instant.getEpochSecond(),
            withMicrosecondsPrecision(instant.getNano())
        );
    }

    public static LocalTime withMicrosecondsPrecision(LocalTime localTime) {
        return LocalTime.ofNanoOfDay(
            withMicrosecondsPrecision(localTime.toNanoOfDay())
        );
    }

    public static LocalDateTime withMicrosecondsPrecision(LocalDateTime localDateTime) {
        return LocalDateTime.of(
            localDateTime.toLocalDate(),
            withMicrosecondsPrecision(localDateTime.toLocalTime())
        );
    }

    public static OffsetTime withMicrosecondsPrecision(OffsetTime offsetTime) {
        return OffsetTime.of(
            withMicrosecondsPrecision(offsetTime.toLocalTime()),
            offsetTime.getOffset()
        );
    }

    public static OffsetDateTime withMicrosecondsPrecision(OffsetDateTime offsetDateTime) {
        return OffsetDateTime.of(
            withMicrosecondsPrecision(offsetDateTime.toLocalDateTime()),
            offsetDateTime.getOffset()
        );
    }

    public static ZonedDateTime withMicrosecondsPrecision(ZonedDateTime zonedDateTime) {
        return ZonedDateTime.of(
            withMicrosecondsPrecision(zonedDateTime.toLocalDateTime()),
            zonedDateTime.getZone()
        );
    }

    private static long withMicrosecondsPrecision(long nanos) {
        val micros = MICROSECONDS.convert(nanos, NANOSECONDS);
        return NANOSECONDS.convert(micros, MICROSECONDS);
    }

    private static int withMicrosecondsPrecision(int nanos) {
        val micros = MICROSECONDS.convert(nanos, NANOSECONDS);
        return toIntExact(NANOSECONDS.convert(micros, MICROSECONDS));
    }


    private DateTimePrecisionUtils() {
    }

}
