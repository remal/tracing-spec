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

package utils.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import java.util.ServiceLoader;
import lombok.val;

public abstract class GsonFactory {

    private static final Gson GSON;

    static {
        val builder = new GsonBuilder()
            .disableHtmlEscaping();
        ServiceLoader.load(TypeAdapterFactory.class).forEach(builder::registerTypeAdapterFactory);
        GSON = builder.create();
    }

    public static Gson getGsonInstance() {
        return GSON;
    }


    private GsonFactory() {
    }

}
