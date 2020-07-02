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

package name.remal.tracingspec.retriever.jaeger;

import static io.grpc.Status.DEADLINE_EXCEEDED;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static name.remal.tracingspec.retriever.jaeger.JaegerIdUtils.encodeJaegerId;
import static name.remal.tracingspec.retriever.jaeger.JaegerSpanConverter.convertJaegerSpanToSpecSpan;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.GetTraceRequest;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.QueryServiceGrpc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JaegerRetriever {

    private static final Duration DEFAULT_QUERY_TIMEOUT = Duration.ofMinutes(1);

    private static final Logger logger = LogManager.getLogger(JaegerRetriever.class);

    private final InetSocketAddress queryServiceAddress;

    private final Duration queryTimeout;

    @VisibleForTesting
    JaegerRetriever(InetSocketAddress queryServiceAddress, Duration queryTimeout) {
        this.queryServiceAddress = queryServiceAddress;
        this.queryTimeout = queryTimeout;
    }

    public JaegerRetriever(InetSocketAddress queryServiceAddress) {
        this(queryServiceAddress, DEFAULT_QUERY_TIMEOUT);
    }

    public List<SpecSpan> retrieveSpansForTrace(String traceId) {
        val traceIdBytes = encodeJaegerId(traceId);

        val channel = ManagedChannelBuilder.forAddress(
            queryServiceAddress.getHostString(),
            queryServiceAddress.getPort()
        ).usePlaintext().build();
        try {

            val queryStub = QueryServiceGrpc.newBlockingStub(channel);
            val request = GetTraceRequest.newBuilder()
                .setTraceId(ByteString.copyFrom(traceIdBytes))
                .build();
            val jaegerSpansChunks = queryStub
                .withDeadlineAfter(queryTimeout.toMillis(), MILLISECONDS)
                .getTrace(request);

            List<SpecSpan> result = new ArrayList<>();
            while (jaegerSpansChunks.hasNext()) {
                val chunk = jaegerSpansChunks.next();
                for (val jaegerSpan : chunk.getSpansList()) {
                    val specSpan = convertJaegerSpanToSpecSpan(jaegerSpan);
                    result.add(specSpan);
                }
            }
            return result;

        } catch (StatusRuntimeException e) {
            if (e.getStatus() == DEADLINE_EXCEEDED) {
                logger.warn("DEADLINE_EXCEEDED gRPC error occurred. It can happen if you use Jaeger <=1.13. Only "
                    + "Jaeger >=1.14 is supported now: https://github.com/remal/tracing-spec/issues/19");
            }
            throw e;

        } finally {
            channel.shutdownNow();
        }
    }

}
