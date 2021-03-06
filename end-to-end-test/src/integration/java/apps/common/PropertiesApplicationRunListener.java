package apps.common;

import static java.lang.System.currentTimeMillis;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class PropertiesApplicationRunListener implements SpringApplicationRunListener, Ordered {

    private static final long START_MILLIS = currentTimeMillis();

    private final Optional<String> serviceName;

    public PropertiesApplicationRunListener(SpringApplication application, String[] args) {
        val baseClassNamePrefix = "apps.";
        serviceName = application.getAllSources().stream()
            .filter(Class.class::isInstance)
            .map(Class.class::cast)
            .map(Class::getName)
            .filter(it -> it.startsWith(baseClassNamePrefix))
            .map(it -> it.substring(baseClassNamePrefix.length()))
            .map(it -> {
                int dotPos = it.lastIndexOf('.');
                return dotPos > 0 ? it.substring(0, dotPos) : it;
            })
            .filter(it -> !it.contains("."))
            .filter(it -> !it.equals("shared"))
            .map(it -> it + "-service")
            .findFirst();
    }

    @Override
    public void environmentPrepared(
        ConfigurableBootstrapContext bootstrapContext,
        ConfigurableEnvironment environment
    ) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("server.port", 0);
        properties.put("spring.cloud.bootstrap.enabled", false);
        serviceName.ifPresent(it -> properties.put("spring.application.name", it));
        serviceName.ifPresent(it -> properties.put("spring.kafka.consumer.group-id", it + '-' + START_MILLIS));

        environment.getPropertySources().addLast(
            new MapPropertySource(
                PropertiesApplicationRunListener.class.getName(),
                properties
            )
        );
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
