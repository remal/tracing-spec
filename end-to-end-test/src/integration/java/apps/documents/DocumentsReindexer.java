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

package apps.documents;

import static com.google.common.collect.Lists.partition;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.BeanUtils.getPropertyDescriptor;

import apps.schemas.Schema;
import apps.schemas.SchemasClient;
import apps.users.User;
import apps.users.UsersClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentsReindexer {

    private final DocumentRepository repository;

    private final ObjectProvider<SchemasClient> schemasClient;

    private final ObjectProvider<UsersClient> usersClient;

    private final ObjectMapper objectMapper;

    private final StaleDocumentsEventSender staleDocumentsEventSender;


    @NewSpan("reindex-documents-of-schema")
    public void reindexDocumentsBySchemaId(String schemaId) {
        val docs = repository.getAllBySchema(schemaId);
        for (val partitionDocs : partition(docs, 50)) {
            staleDocumentsEventSender.sendStaleDocumentsEvent(ImmutableStaleDocumentsEvent.builder()
                .ids(partitionDocs.stream().map(Document::getId).collect(toList()))
                .build()
            );
        }
    }


    @NewSpan("reindex-documents")
    public void reindexDocuments(List<Document> documents) {
        val cache = new Cache();
        for (val doc : documents) {
            reindexDocument(doc, cache);
        }
    }

    @SneakyThrows
    private void reindexDocument(Document doc, Cache cache) {
        val oldContent = doc.getContent();
        ObjectNode content = objectMapper.valueToTree(oldContent);
        val schema = cache.getSchema(doc.getId().getSchema());
        for (val ref : schema.getReferences()) {
            val idNode = Optional.ofNullable(content.get(ref.getIdField()))
                .filter(it -> !it.isNull())
                .orElse(null);
            if (idNode == null) {
                continue;
            }

            final Object data;
            val dataType = ref.getDataType();
            if (dataType.equals("user")) {
                val id = idNode.asInt();
                data = cache.getUser(id);
            } else {
                throw new IllegalStateException("Unsupported data type: " + dataType);
            }

            for (Entry<String, String> mapping : ref.getFieldMappings().entrySet()) {
                val fromField = mapping.getKey();
                val toField = mapping.getValue();

                val descriptor = Optional.ofNullable(getPropertyDescriptor(data.getClass(), fromField))
                    .filter(it -> it.getReadMethod() != null)
                    .orElseThrow(() -> new IllegalStateException(format(
                        "%s doesn't have readable property %s",
                        data.getClass(),
                        fromField
                    )));
                val readMethod = requireNonNull(descriptor.getReadMethod());

                val value = readMethod.invoke(data);
                val valueNode = objectMapper.valueToTree(value);
                content.set(toField, valueNode);
            }
        }

        if (!content.equals(oldContent)) {
            repository.save(ImmutableDocument.builder()
                .from(doc)
                .content(content)
                .build()
            );
        }
    }

    @NotThreadSafe
    private class Cache {

        private final Map<String, Schema> schemas = new HashMap<>();

        public Schema getSchema(String schemaId) {
            return schemas.computeIfAbsent(schemaId, schemasClient.getObject()::getSchema);
        }

        private final Map<Integer, User> users = new HashMap<>();

        public User getUser(int userId) {
            return users.computeIfAbsent(userId, usersClient.getObject()::getUser);
        }

    }

}
