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

package name.remal.tracingspec.retriever.zipkin.internal.retrofit;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;
import lombok.val;
import name.remal.gradle_plugins.api.ExcludeFromCodeCoverage;
import org.jetbrains.annotations.ApiStatus.Internal;
import retrofit2.Call;

@Internal
@ExcludeFromCodeCoverage
class CommonCallAdapter extends BaseCallAdapter {

    public CommonCallAdapter(Type responseType) {
        super(responseType);
    }

    @Override
    public Object adapt(Call<Object> call) {
        val response = executeCall(call);
        if (response.isSuccessful()) {
            return requireNonNull(response.body());

        } else {
            throw createStatusException(call, response);
        }
    }

}
