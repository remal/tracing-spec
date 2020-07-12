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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import com.google.common.reflect.TypeToken;
import java.lang.annotation.Annotation;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

class OptionalCallAdapterFactoryTest {

    private final OptionalCallAdapterFactory factory = new OptionalCallAdapterFactory();

    private final Retrofit retrofit = mock(Retrofit.class, invocation -> {
        throw new UnsupportedOperationException();
    });

    @Test
    void returns_right_adapter() {
        val type = new TypeToken<Optional<String>>() { }.getType();
        val adapter = factory.get(type, new Annotation[0], retrofit);
        assertThat(adapter, notNullValue());
        assertThat(adapter, isA(OptionalCallAdapter.class));
        assertThat(adapter.responseType(), is(type));
    }

    @Test
    void returns_null_for_incorrect_type() {
        val adapter = factory.get(CharSequence.class, new Annotation[0], retrofit);
        assertThat(adapter, nullValue());
    }

}
