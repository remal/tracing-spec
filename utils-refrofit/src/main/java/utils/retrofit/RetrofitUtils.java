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

package utils.retrofit;

import lombok.val;
import retrofit2.Retrofit;

public interface RetrofitUtils {

    @SuppressWarnings("java:S1872")
    static Retrofit.Builder newRetrofitBuilder() {
        try {
            return new Retrofit.Builder();

        } catch (ExceptionInInitializerError exception) {
            val cause = exception.getCause();
            if (cause == null) {
                throw exception;
            }

            if ("java.lang.reflect.InaccessibleObjectException".equals(cause.getClass().getName())) {
                exception.addSuppressed(new RuntimeException(
                    "Looks like you denied illegal reflective access (by passing '--illegal-access=deny' command line"
                        + " argument). However, Retrofit library has illegal reflective access bug, when running on"
                        + " Java 9-13 (https://github.com/square/retrofit/issues/3341)."
                        + " This bug was fixed in Java 14. You can upgrade your Java runtime to version 14 or to"
                        + " enable illegal reflective access by NOT passing '--illegal-access=deny' command line"
                        + " argument."
                ));
            }

            throw exception;
        }
    }

}
