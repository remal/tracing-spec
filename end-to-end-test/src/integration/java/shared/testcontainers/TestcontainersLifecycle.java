package shared.testcontainers;

import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.testcontainers.lifecycle.Startable;

@RequiredArgsConstructor
class TestcontainersLifecycle implements DisposableBean {

    private static final Logger logger = LogManager.getLogger(TestcontainersLifecycle.class);

    private final ApplicationContext applicationContext;


    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    private void stopContainers() {
        if (isStopped.compareAndSet(false, true)) {

            val startableBeans = applicationContext.getBeansOfType(Startable.class).values();
            if (!startableBeans.isEmpty()) {
                logger.info("Stopping {} containers", startableBeans.size());
            }

            startableBeans.forEach(Startable::stop);

        }
    }


    @Override
    public void destroy() {
        stopContainers();
    }

}
