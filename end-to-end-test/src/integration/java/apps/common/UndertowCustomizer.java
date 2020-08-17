/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
