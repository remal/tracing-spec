package apps.schemas;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface SchemasApi {

    @Operation(summary = "Save schema")
    @PostMapping("/schemas")
    @ResponseStatus(NO_CONTENT)
    void saveSchema(@RequestBody Schema schema);

    @Operation(summary = "Get schema by ID")
    @GetMapping("/schemas/{id}")
    Schema getSchema(@PathVariable String id);

}
