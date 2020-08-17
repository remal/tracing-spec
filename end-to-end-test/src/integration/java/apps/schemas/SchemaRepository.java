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

import apps.common.repository.AbstractInMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S1171")
public class SchemaRepository extends AbstractInMemoryRepository<String, Schema> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    protected void onEntityChanged(Schema entity) {
        kafkaTemplate.send(SCHEMA_CHANGED_TOPIC, ImmutableSchemaChangedEvent.builder()
            .id(entity.getId())
            .build()
        );
    }


    {
        save(ImmutableSchema.builder()
            .id("task")
            .addReference(ImmutableSchemaReference.builder()
                .dataType("user")
                .idField("userId")
                .putFieldMapping("email", "userEmail")
                .build()
            )
            .build()
        );
    }

}
