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

import apps.schemas.ImmutableSchemaReference.SchemaReferenceBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashSet;
import java.util.Map;
import lombok.val;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = SchemaReferenceBuilder.class)
public interface SchemaReference {

    String getDataType();

    String getIdField();

    Map<String, String> getFieldMappings();


    @Value.Check
    default void validate() {
        val targetFields = getFieldMappings().values();
        if (targetFields.contains(getIdField())) {
            throw new IllegalStateException("fieldMappings targets contain idField: " + getIdField());
        }
        if (new HashSet<>(targetFields).size() != targetFields.size()) {
            throw new IllegalStateException("fieldMappings targets are not unique");
        }
    }

}