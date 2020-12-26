package apps.schemas;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("schemas-service")
public interface SchemasClient extends SchemasApi {
}
