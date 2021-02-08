package name.remal.tracingspec.retriever.jaeger;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getClassSimpleName;
import static utils.gson.GsonFactory.getGsonInstance;

import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.validation.Valid;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.SpecSpansRetriever;
import name.remal.tracingspec.retriever.jaeger.internal.JaegerTrace;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.VisibleForTesting;
import utils.okhttp.ConnectionCloseHeaderInterceptor;
import utils.okhttp.ErroneousResponseInterceptor;
import utils.okhttp.HttpLoggingInterceptor;
import utils.okhttp.RequiredTextResponseBodyInterceptor;

@ToString
public class JaegerSpecSpansRetriever implements SpecSpansRetriever {

    @Valid
    private final JaegerSpecSpansRetrieverProperties properties;

    public JaegerSpecSpansRetriever(JaegerSpecSpansRetrieverProperties properties) {
        this.properties = properties;
    }

    @Override
    @SneakyThrows
    public List<SpecSpan> retrieveSpecSpansForTrace(String traceId) {
        lastErroneousJson.set(null);

        val jaegerUrl = properties.getUrl();
        if (jaegerUrl == null) {
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
            .url(requireNonNull(HttpUrl.get(jaegerUrl))
                .newBuilder()
                .addPathSegments("api/traces/" + traceId)
                .build()
            )
            .build();
        val call = client.newCall(request);
        val response = call.execute();
        val json = requireNonNull(response.body()).string();

        try {
            val jaegerTrace = getGsonInstance().fromJson(json, JaegerTrace.class);
            return JaegerSpanConverter.convertJaegerTraceToSpecSpans(jaegerTrace);

        } catch (Throwable e) {
            if (e.getClass().getSimpleName().equalsIgnoreCase(getClassSimpleName(JsonSyntaxException.class))) {
                lastErroneousJson.set(json);
            }
            throw e;
        }
    }

    private final AtomicReference<String> lastErroneousJson = new AtomicReference<>();

    @VisibleForTesting
    @Nullable
    String getLastErroneousJson() {
        return lastErroneousJson.get();
    }

}
