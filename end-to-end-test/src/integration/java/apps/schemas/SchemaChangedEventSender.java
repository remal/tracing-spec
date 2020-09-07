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

package apps.schemas;

import static apps.schemas.SchemaChangedEvent.SCHEMA_CHANGED_TOPIC;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchemaChangedEventSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @NewSpan("send-schema-changed-event")
    public void sendSchemaChangedEvent(SchemaChangedEvent event) {
        kafkaTemplate.send(SCHEMA_CHANGED_TOPIC, event);
    }

}
