package apps.common;

import static java.util.Collections.emptyMap;

import lombok.val;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import shared.SharedDiscoveryClient;

@Component
public class ServiceInstanceRegisterer implements ApplicationListener<WebServerInitializedEvent> {

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        val context = event.getApplicationContext();
        val serviceId = context.getEnvironment().getRequiredProperty("spring.application.name");
        val server = event.getWebServer();
        context.getBeanProvider(SharedDiscoveryClient.class).ifAvailable(sharedDiscoveryClient ->
            sharedDiscoveryClient.registerServiceInstance(new DefaultServiceInstance(
                null,
                serviceId,
                "localhost",
                server.getPort(),
                false,
                emptyMap()
            ))
        );
    }

}
