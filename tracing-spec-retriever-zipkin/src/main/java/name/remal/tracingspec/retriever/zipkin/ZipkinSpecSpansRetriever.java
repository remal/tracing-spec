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

import java.util.List;
import lombok.ToString;
import name.remal.tracingspec.model.SpecSpan;
import name.remal.tracingspec.retriever.SpecSpansRetriever;

@ToString
public class ZipkinSpecSpansRetriever implements SpecSpansRetriever {

    private final ZipkinSpecSpansRetrieverProperties properties;

    public ZipkinSpecSpansRetriever(ZipkinSpecSpansRetrieverProperties properties) {
        this.properties = properties;
    }

    @Override
    public List<SpecSpan> retrieveSpecSpansForTrace(String traceId) {
        throw new UnsupportedOperationException();
    }

}
