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

package name.remal.tracingspec.renderer.jackson;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.Instantiatable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.io.Serializable;

public class JsonTracingSpecRenderer extends AbstractJacksonTracingSpecRenderer {

    public JsonTracingSpecRenderer(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public JsonTracingSpecRenderer() {
        super(JsonMapper.builder()
            .defaultPrettyPrinter(new CustomJsonPrettyPrinter())
            .enable(INDENT_OUTPUT)
            .findAndAddModules()
            .build()
        );
    }

    @Override
    public String getRendererName() {
        return "json";
    }


    @SuppressWarnings("java:S4144")
    private static class CustomJsonPrettyPrinter
        implements PrettyPrinter, Instantiatable<CustomJsonPrettyPrinter>, Serializable {

        private static final long serialVersionUID = 1;

        @Override
        public CustomJsonPrettyPrinter createInstance() {
            return new CustomJsonPrettyPrinter();
        }

        private static final String INDENT = "  ";

        private transient int depth;

        private void writeIndent(JsonGenerator gen) throws IOException {
            for (int i = 0; i < depth; ++i) {
                gen.writeRaw(INDENT);
            }
        }

        @Override
        public void writeRootValueSeparator(JsonGenerator gen) throws IOException {
            gen.writeRaw("\n");
        }

        @Override
        public void writeStartObject(JsonGenerator gen) throws IOException {
            gen.writeRaw("{");
            ++depth;
        }

        @Override
        public void beforeObjectEntries(JsonGenerator gen) throws IOException {
            gen.writeRaw("\n");
            writeIndent(gen);
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {
            gen.writeRaw(": ");
        }

        @Override
        public void writeObjectEntrySeparator(JsonGenerator gen) throws IOException {
            gen.writeRaw(",\n");
            writeIndent(gen);
        }

        @Override
        public void writeEndObject(JsonGenerator gen, int nrOfEntries) throws IOException {
            --depth;
            if (1 <= nrOfEntries) {
                gen.writeRaw("\n");
                writeIndent(gen);
            }
            gen.writeRaw("}");
        }

        @Override
        public void writeStartArray(JsonGenerator gen) throws IOException {
            gen.writeRaw("[");
            ++depth;
        }

        @Override
        public void beforeArrayValues(JsonGenerator gen) throws IOException {
            gen.writeRaw("\n");
            writeIndent(gen);
        }

        @Override
        public void writeArrayValueSeparator(JsonGenerator gen) throws IOException {
            gen.writeRaw(",\n");
            writeIndent(gen);
        }

        @Override
        public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {
            --depth;
            if (1 <= nrOfValues) {
                gen.writeRaw("\n");
                writeIndent(gen);
            }
            gen.writeRaw("]");
        }

    }

}
