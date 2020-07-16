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

import java.lang.reflect.Type;
import java.util.Optional;
import lombok.val;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class OptionalCallAdapter extends BaseCallAdapter {

    public OptionalCallAdapter(Type responseType) {
        super(responseType);
    }

    @Override
    @SuppressWarnings("java:S109")
    public Object adapt(Call<Object> call) {
        val response = executeCall(call);
        if (response.isSuccessful()) {
            long contentLength = Optional.ofNullable(response.raw().body())
                .map(ResponseBody::contentLength)
                .orElse(-1L);
            if (contentLength == 0L) {
                return Optional.empty();
            }
            if (contentLength <= 0 && response.code() == 204) {
                return Optional.empty();
            }

            val body = response.body();
            if (body == null) {
                return Optional.empty();
            }

            return body;

        } else if (response.code() == 404) {
            return Optional.empty();

        } else {
            throw createStatusException(call, response);
        }
    }

}
