package apps.documents;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface DocumentsApi {

    @Operation(summary = "Save document")
    @PostMapping("/documents")
    @ResponseStatus(NO_CONTENT)
    void saveDocument(@RequestBody Document document);

    @Operation(summary = "Get all documents by schema ID")
    @GetMapping("/documents/{schemaId}")
    List<Document> getAllDocumentsBySchema(@PathVariable String schemaId);

}
