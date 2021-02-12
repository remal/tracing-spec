package apps.users;

import apps.common.repository.Entity;
import apps.users.ImmutableUser.UserBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = UserBuilder.class)
public interface User extends Entity<Integer> {

    String getFullName();

    String getEmail();

}
