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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static name.remal.tracingspec.retriever.jaeger.JaegerIdUtils.encodeJaegerId;
import static name.remal.tracingspec.retriever.jaeger.JaegerSpanConverter.convertJaegerSpanToSpecSpan;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.GetTraceRequest;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.QueryServiceGrpc;

public class JaegerRetriever {

    private static final Duration QUERY_TIMEOUT = Duration.ofMinutes(1);

    private final InetSocketAddress queryServiceAddress;

    public JaegerRetriever(InetSocketAddress queryServiceAddress) {
        this.queryServiceAddress = queryServiceAddress;
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
                .withDeadlineAfter(QUERY_TIMEOUT.toMillis(), MILLISECONDS)
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

        } finally {
            channel.shutdownNow();
        }
    }

}
