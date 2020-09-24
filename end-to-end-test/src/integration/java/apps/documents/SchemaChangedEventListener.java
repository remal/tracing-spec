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

import static apps.schemas.SchemaChangedEvent.SCHEMA_CHANGED_TOPIC;

import apps.schemas.SchemaChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchemaChangedEventListener {

    private final DocumentsReindexer documentsReindexer;

    @KafkaListener(topics = SCHEMA_CHANGED_TOPIC, autoStartup = "true")
    @NewSpan("process-schema-changed-event")
    public void onSchemaChangedEvent(SchemaChangedEvent event) {
        documentsReindexer.reindexDocumentsBySchemaId(event.getId());
    }

}
