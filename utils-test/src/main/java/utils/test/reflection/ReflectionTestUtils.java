package utils.test.reflection;

import static java.lang.String.format;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import lombok.val;

@SuppressWarnings("UnstableApiUsage")
public interface ReflectionTestUtils {

    @SuppressWarnings("unchecked")
    static <T, R> Class<R> getParameterizedTypeArgumentClass(
        Class<T> childClass,
        Class<? super T> superClass,
        int index
    ) {
        if (index < 0) {
            throw new IllegalArgumentException("index mustn't be less than 0: " + index);
        }

        val superType = TypeToken.of(childClass).getSupertype(superClass).getType();
        if (superType instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType) superType;
            val arguments = parameterizedType.getActualTypeArguments();
            if (index < arguments.length) {
                return (Class<R>) TypeToken.of(arguments[index]).getRawType();
            } else {
                throw new IllegalArgumentException(format(
                    "%s has only %d arguments, but index #%d is requested",
                    superType,
                    arguments.length,
                    index
                ));
            }
        } else {
            throw new IllegalStateException(superType + " is not a ParameterizedType");
        }
    }

}
