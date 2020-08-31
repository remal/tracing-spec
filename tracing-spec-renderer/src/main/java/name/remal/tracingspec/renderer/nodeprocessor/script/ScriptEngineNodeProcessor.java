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

package name.remal.tracingspec.renderer.nodeprocessor.script;

import java.util.function.Function;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.renderer.AbstractSpecSpanNodeProcessor;

public class ScriptEngineNodeProcessor extends AbstractSpecSpanNodeProcessor {

    private final NodeFunction nodeFunction;

    @SneakyThrows
    public ScriptEngineNodeProcessor(String engineName, String script) {
        val manager = new ScriptEngineManager();
        val engine = manager.getEngineByName(engineName);

        Function<SpecSpanNode, Bindings> createBindings = node -> {
            val bindings = engine.createBindings();
            bindings.put("node", node);
            return bindings;
        };

        if (engine instanceof Compilable) {
            val compiledScript = ((Compilable) engine).compile(script);
            nodeFunction = node -> {
                val bindings = createBindings.apply(node);
                compiledScript.eval(bindings);
            };
        } else {
            nodeFunction = node -> {
                val bindings = createBindings.apply(node);
                engine.eval(script, bindings);
            };
        }
    }

    @Override
    public void processNode(SpecSpanNode node) throws Throwable {
        nodeFunction.processNode(node);
    }


    @FunctionalInterface
    private interface NodeFunction {
        void processNode(SpecSpanNode node) throws Throwable;
    }

}
