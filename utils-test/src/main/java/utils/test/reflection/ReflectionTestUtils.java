/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.test.reflection;

import static java.lang.String.format;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import lombok.val;

@SuppressWarnings("UnstableApiUsage")
public interface ReflectionTestUtils {

    static <T> Class<?> getParameterizedTypeArgumentClass(Class<T> childClass, Class<? super T> superClass, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index mustn't be less than 0: " + index);
        }

        val superType = TypeToken.of(childClass).getSupertype(superClass).getType();
        if (superType instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType) superType;
            val arguments = parameterizedType.getActualTypeArguments();
            if (index < arguments.length) {
                return TypeToken.of(arguments[index]).getRawType();
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
