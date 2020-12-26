package apps.documents;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DocumentsController implements DocumentsApi {

    private final DocumentRepository repository;

    @Override
    public void saveDocument(Document document) {
        repository.save(document);
    }

    @Override
    public List<Document> getAllDocumentsBySchema(String schemaId) {
        return repository.getAllBySchema(schemaId);
    }

}
