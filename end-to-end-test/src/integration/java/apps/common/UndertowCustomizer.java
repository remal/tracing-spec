package apps.common;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import lombok.val;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class UndertowCustomizer implements WebServerFactoryCustomizer<UndertowServletWebServerFactory>, Ordered {

    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            val untypedWsDeploymentInfo = deploymentInfo.getServletContextAttributes()
                .get(WebSocketDeploymentInfo.ATTRIBUTE_NAME);
            if (untypedWsDeploymentInfo instanceof WebSocketDeploymentInfo) {
                val wsDeploymentInfo = (WebSocketDeploymentInfo) untypedWsDeploymentInfo;
                if (wsDeploymentInfo.getBuffers() == null) {
                    wsDeploymentInfo.setBuffers(
                        new DefaultByteBufferPool(
                            Boolean.getBoolean("io.undertow.websockets.direct-buffers"),
                            1024,
                            100,
                            12
                        )
                    );
                }
            }
        });
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

}
