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

import apps.documents.ImmutableStaleDocumentsEvent.StaleDocumentsEventBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@Value.Immutable
@JsonDeserialize(builder = StaleDocumentsEventBuilder.class)
public interface StaleDocumentsEvent {

    String STALE_DOCUMENTS_TOPIC = "stale-documents";

    List<DocumentId> getIds();


    @Value.Check
    @OverrideOnly
    default void validate() {
        if (getIds().isEmpty()) {
            throw new IllegalStateException("ids must not be empty");
        }
    }

}