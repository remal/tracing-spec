package name.remal.tracingspec.retriever.zipkin;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static utils.gson.GsonFactory.getGsonInstance;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import javax.validation.Valid;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import name.remal.tracingspec.retriever.zipkin.internal.ZipkinSpan;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import utils.okhttp.ConnectionCloseHeaderInterceptor;
import utils.okhttp.ErroneousResponseInterceptor;
import utils.okhttp.HttpLoggingInterceptor;
import utils.okhttp.RequiredTextResponseBodyInterceptor;

@ToString
public class ZipkinSpecSpansRetriever implements SpecSpansRetriever {

    private static final Type ZIPKIN_SPANS_TYPE = new TypeToken<List<ZipkinSpan>>() { }.getType();

    @Valid
    private final ZipkinSpecSpansRetrieverProperties properties;

    public ZipkinSpecSpansRetriever(ZipkinSpecSpansRetrieverProperties properties) {
        this.properties = properties;
    }

    @Override
    @SneakyThrows
    public List<SpecSpan> retrieveSpecSpansForTrace(String traceId) {
        val zipkinUrl = properties.getUrl();
        if (zipkinUrl == null) {
            throw new IllegalStateException("properties.url must not be null");
        }

        val client = new OkHttpClient.Builder()
            .connectTimeout(properties.getConnectTimeoutMillis(), MILLISECONDS)
            .writeTimeout(properties.getWriteTimeoutMillis(), MILLISECONDS)
            .readTimeout(properties.getReadTimeoutMillis(), MILLISECONDS)
            .addInterceptor(new RequiredTextResponseBodyInterceptor())
            .addInterceptor(new ErroneousResponseInterceptor())
            .addInterceptor(new ConnectionCloseHeaderInterceptor())
            .addInterceptor(new HttpLoggingInterceptor())
            .build();

        val request = new Request.Builder()
            .url(requireNonNull(HttpUrl.get(zipkinUrl))
                .newBuilder()
                .addPathSegments("api/v2/trace/" + traceId)
                .build()
            )
            .build();
        val call = client.newCall(request);
        val response = call.execute();
        val json = requireNonNull(response.body()).string();

        List<ZipkinSpan> zipkinSpans = getGsonInstance().fromJson(json, ZIPKIN_SPANS_TYPE);
        return zipkinSpans.stream()
            .map(ZipkinSpanConverter::convertZipkinSpanToSpecSpan)
            .collect(toList());
    }

}
