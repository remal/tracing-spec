package apps.users;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("users-service")
public interface UsersClient extends UsersApi {
}
