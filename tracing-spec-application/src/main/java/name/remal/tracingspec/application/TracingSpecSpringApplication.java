package name.remal.tracingspec.application;

import static name.remal.tracingspec.application.ExitException.findExitException;
import static org.springframework.boot.Banner.Mode.OFF;
import static org.springframework.boot.WebApplicationType.NONE;

import lombok.val;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TracingSpecSpringApplication {

    private static final Logger logger = LogManager.getLogger(TracingSpecSpringApplication.class);

    /**
     * Same as {@link #main}, but without calling {@link System#exit(int)} on error.
     */
    public static void run(String... args) {
        ConfigurableApplicationContext context = null;
        try {
            val application = new SpringApplication(TracingSpecSpringApplication.class);
            application.setBannerMode(OFF);
            application.setLogStartupInfo(false);
            application.setWebApplicationType(NONE);
            application.setRegisterShutdownHook(false);
            application.setLazyInitialization(true);

            context = application.run(args);

            val runner = context.getBean(TracingSpecApplicationRunner.class);
            runner.run(args);

        } finally {
            if (context != null) {
                if (context.isRunning()) {
                    context.stop();
                }
                if (context.isActive()) {
                    context.close();
                }
            }
        }
    }

    public static void main(String... args) {
        try {
            run(args);

        } catch (Throwable exception) {
            val exitException = findExitException(exception);
            if (exitException != null) {
                val message = exitException.getMessage();
                if (message != null && !message.isEmpty()) {
                    logger.error(message);
                }
                System.exit(exitException.getExitCode());
            } else {
                throw exception;
            }
        }
    }

}
