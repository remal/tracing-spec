package apps.users;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface UsersApi {

    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{id}")
    User getUser(@PathVariable int id);

}
