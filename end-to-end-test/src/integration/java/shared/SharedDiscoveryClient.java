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

package shared;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.ToString;
import lombok.val;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

@Component
@ToString
public class SharedDiscoveryClient implements DiscoveryClient {

    private final Map<String, ServiceInstance> serviceInstances = new ConcurrentHashMap<>();

    public void registerServiceInstance(ServiceInstance serviceInstance) {
        serviceInstances.put(
            requireNonNull(serviceInstance.getServiceId(), "serviceId"),
            serviceInstance
        );
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        val serviceInstance = serviceInstances.get(serviceId);
        if (serviceInstance != null) {
            return singletonList(serviceInstance);
        } else {
            return emptyList();
        }
    }

    @Override
    public List<String> getServices() {
        return new ArrayList<>(serviceInstances.keySet());
    }

    @Override
    public String description() {
        return SharedDiscoveryClient.class.getName();
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

}
