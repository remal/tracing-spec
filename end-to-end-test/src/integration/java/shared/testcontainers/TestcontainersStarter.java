package shared.testcontainers;

import static java.util.Collections.synchronizedMap;

import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

public class TestcontainersStarter {

    private static final Logger logger = LogManager.getLogger(TestcontainersStarter.class);

    private final Object mutex = new Object[0];
    private final Map<Container<?>, Status> statuses = synchronizedMap(new IdentityHashMap<>());

    public void start(GenericContainer<?> container) {
        if (statuses.putIfAbsent(container, Status.STARTING) != null) {
            return;
        }

        val thread = new Thread(() -> {
            try {
                container.start();
                statuses.put(container, Status.STARTED);

            } catch (Throwable exception) {
                statuses.put(container, Status.FAILED);
                throw exception;

            } finally {
                synchronized (mutex) {
                    mutex.notifyAll();
                }
            }
        });
        thread.setName("container starter: " + container.getDockerImageName());
        thread.setDaemon(true);
        thread.start();
    }

    @SneakyThrows
    public <T extends GenericContainer<?>> T startAndWait(T container) {
        start(container);

        Status status;
        while (true) {
            status = statuses.get(container);
            if (status == Status.STARTED || status == Status.FAILED) {
                break;
            }

            logger.info(() -> "Waiting for the container to start: " + container.getDockerImageName());

            synchronized (mutex) {
                mutex.wait(Duration.ofMinutes(1).toMillis());
            }
        }

        if (status == Status.FAILED) {
            throw new IllegalStateException("Start failed: " + container);
        }

        return container;
    }

    private enum Status {
        STARTING,
        STARTED,
        FAILED,
    }

}
