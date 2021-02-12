package apps.common;

import java.util.Optional;
import lombok.val;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class FeignClientsRegisterer implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        val context = event.getApplicationContext();

        val parentBeanFactory = Optional.ofNullable(context.getParent())
            .map(ApplicationContext::getAutowireCapableBeanFactory)
            .filter(ConfigurableBeanFactory.class::isInstance)
            .map(ConfigurableBeanFactory.class::cast)
            .orElse(null);
        if (parentBeanFactory == null) {
            return;
        }

        val beanNames = context.getBeanNamesForAnnotation(FeignClient.class);
        for (val beanName : beanNames) {
            val bean = context.getBean(beanName);
            parentBeanFactory.registerSingleton(beanName + "#parent", bean);
        }
    }

}
