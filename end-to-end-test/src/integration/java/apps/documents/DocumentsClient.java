package apps.documents;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("documents-service")
public interface DocumentsClient extends DocumentsApi {
}
