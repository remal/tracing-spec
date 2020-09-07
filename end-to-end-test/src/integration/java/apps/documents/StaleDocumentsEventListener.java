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

import static apps.documents.StaleDocumentsEvent.STALE_DOCUMENTS_TOPIC;
import static java.util.stream.Collectors.toList;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaleDocumentsEventListener {

    private final DocumentRepository repository;

    private final DocumentsReindexer documentsReindexer;

    @KafkaListener(topics = STALE_DOCUMENTS_TOPIC)
    @NewSpan("process-stale-documents-event")
    public void onDocumentsShouldBeUpdatedEvent(StaleDocumentsEvent event) {
        val docs = event.getIds().stream()
            .distinct()
            .map(repository::getById)
            .collect(toList());
        documentsReindexer.reindexDocuments(docs);
    }

}
