package apps.schemas;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SchemasController implements SchemasApi {

    private final SchemaRepository repository;

    @Override
    public void saveSchema(Schema schema) {
        repository.save(schema);
    }

    @Override
    public Schema getSchema(String id) {
        return repository.getById(id);
    }

}
