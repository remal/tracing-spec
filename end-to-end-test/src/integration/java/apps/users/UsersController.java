package apps.users;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UsersController implements UsersApi {

    private final UserRepository repository;

    @Override
    public User getUser(int id) {
        return repository.getById(id);
    }

}
