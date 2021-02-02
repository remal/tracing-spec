package apps.users;

import apps.common.repository.AbstractInMemoryRepository;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("java:S1171")
public class UserRepository extends AbstractInMemoryRepository<Integer, User> {

    {
        for (int id = 1; id <= 1_000_000; ++id) {
            val entity = ImmutableUser.builder()
                .id(id)
                .fullName(FAKER.name().fullName())
                .email(FAKER.internet().safeEmailAddress())
                .build();
            save(entity);
        }
    }

}
