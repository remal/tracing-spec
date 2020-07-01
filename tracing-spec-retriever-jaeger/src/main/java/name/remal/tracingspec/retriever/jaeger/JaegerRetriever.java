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

import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static name.remal.tracingspec.retriever.jaeger.JaegerIdUtils.encodeJaegerId;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannelBuilder;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.val;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.GetTraceRequest;
import name.remal.tracingspec.retriever.jaeger.internal.grpc.QueryServiceGrpc;

public class JaegerRetriever {

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
            val jaegerSpansChunks = queryStub.getTrace(request);
            return StreamSupport.stream(spliteratorUnknownSize(jaegerSpansChunks, ORDERED), false)
                .flatMap(chunk -> chunk.getSpansList().stream())
                .map(JaegerSpanConverter::convertJaegerSpanToSpecSpan)
                .collect(toList());

        } finally {
            channel.shutdownNow();
        }
    }

}
