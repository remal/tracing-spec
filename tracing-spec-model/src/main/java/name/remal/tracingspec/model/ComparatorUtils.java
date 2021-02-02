package name.remal.tracingspec.model;

import javax.annotation.Nullable;

interface ComparatorUtils {

    static <T extends Comparable<T>> int compareNullLast(@Nullable T value1, @Nullable T value2) {
        if (value1 == null && value2 == null) {
            return 0;
        } else if (value1 != null && value2 != null) {
            return value1.compareTo(value2);
        } else if (value1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

}
