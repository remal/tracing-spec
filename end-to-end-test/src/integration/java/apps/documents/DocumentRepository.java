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

import static java.util.stream.Collectors.toList;

import apps.common.repository.AbstractInMemoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("java:S1171")
public class DocumentRepository extends AbstractInMemoryRepository<DocumentId, Document> {

    public List<Document> getAllBySchema(String schema) {
        return getAll().stream()
            .filter(doc -> doc.getId().getSchema().equals(schema))
            .collect(toList());
    }

}
