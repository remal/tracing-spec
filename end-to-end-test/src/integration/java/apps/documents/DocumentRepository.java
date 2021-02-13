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
