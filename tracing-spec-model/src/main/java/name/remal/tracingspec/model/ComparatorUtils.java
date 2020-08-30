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

package name.remal.tracingspec.model;

import javax.annotation.Nullable;

interface ComparatorUtils {

    static <T extends Comparable<T>> int compareNullLast(@Nullable T obj1, @Nullable T obj2) {
        if (obj1 == null && obj2 == null) {
            return 0;
        } else if (obj1 != null && obj2 != null) {
            return obj1.compareTo(obj2);
        } else if (obj1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

}