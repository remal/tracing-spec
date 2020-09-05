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

package name.remal.tracingspec.renderer.nodeprocessor;

import static name.remal.tracingspec.model.SpecSpanKind.CLIENT;
import static name.remal.tracingspec.model.SpecSpanKind.SERVER;

import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.renderer.AbstractSpecSpanNodeProcessor;

public class AppendServerToClientNodeProcessor extends AbstractSpecSpanNodeProcessor {

    @Override
    public void processNode(SpecSpanNode parent) {
        val children = parent.getChildren();
        if (children.size() != 1) {
            return;
        }

        val child = children.get(0);
        if (parent.getKind() == CLIENT && child.getKind() == SERVER) {
            if (parent.getName() != null && child.getName() != null) {
                if (child.getName().startsWith(parent.getName())) {
                    parent.setName(child.getName());
                }
            }

            if (parent.getRemoteServiceName() == null) {
                parent.setRemoteServiceName(child.getServiceName());
            }

            parent.append(child);
            parent.sortChildren();
        }
    }

}
