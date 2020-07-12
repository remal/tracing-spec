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

package name.remal.tracingspec.retriever.zipkin;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinApi;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import utils.okhttp.ConnectionCloseHeaderInterceptor;
import utils.okhttp.HttpLoggingInterceptor;
import utils.retrofit.CommonCallAdapterFactory;
import utils.retrofit.JsonBodyConverters;
import utils.retrofit.OptionalCallAdapterFactory;

@ToString
public class ZipkinSpecSpansRetriever implements SpecSpansRetriever {

    private final ZipkinSpecSpansRetrieverProperties properties;

    public ZipkinSpecSpansRetriever(ZipkinSpecSpansRetrieverProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<SpecSpan> retrieveSpecSpansForTrace(String traceId) {
        val zipkinUrl = properties.getUrl();
        if (zipkinUrl == null) {
            throw new IllegalStateException("properties.url must not be null");
        }

        val retrofit = new Retrofit.Builder()
            .baseUrl(zipkinUrl)
            .client(new OkHttpClient.Builder()
                .connectTimeout(properties.getConnectTimeoutMillis(), MILLISECONDS)
                .writeTimeout(properties.getWriteTimeoutMillis(), MILLISECONDS)
                .readTimeout(properties.getReadTimeoutMillis(), MILLISECONDS)
                .addInterceptor(new ConnectionCloseHeaderInterceptor())
                .addInterceptor(new HttpLoggingInterceptor())
                .build()
            )
            .addConverterFactory(new JsonBodyConverters())
            .addCallAdapterFactory(new OptionalCallAdapterFactory())
            .addCallAdapterFactory(new CommonCallAdapterFactory())
            .validateEagerly(true)
            .build();

        val zipkinApi = retrofit.create(ZipkinApi.class);

        return zipkinApi.getTraceSpans(traceId).stream()
            .map(ZipkinSpanConverter::convertZipkinSpanToSpecSpan)
            .collect(toList());
    }

}
