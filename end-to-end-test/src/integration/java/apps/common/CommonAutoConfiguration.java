package apps.common;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
@Role(ROLE_INFRASTRUCTURE)
@ComponentScan
public class CommonAutoConfiguration {
}
