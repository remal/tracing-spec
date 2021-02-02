package name.remal.tracingspec.renderer.processor.script;

import static javax.script.ScriptContext.ENGINE_SCOPE;

import java.util.Optional;
import java.util.function.Function;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptEngineManager;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.tracingspec.model.SpecSpanNode;
import name.remal.tracingspec.model.SpecSpansGraph;
import name.remal.tracingspec.renderer.SpecSpanNodeProcessor;
import name.remal.tracingspec.renderer.SpecSpansGraphProcessor;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

public class ScriptEngineProcessor implements SpecSpansGraphProcessor, SpecSpanNodeProcessor {

    private final GraphFunction graphFunction;
    private final NodeFunction nodeFunction;

    @SneakyThrows
    public ScriptEngineProcessor(String language, String script) {
        val manager = new ScriptEngineManager();
        val engine = manager.getEngineByName(language);

        Optional.ofNullable(engine.getBindings(ENGINE_SCOPE)).ifPresent(this::processEngineBindings);

        {
            Function<SpecSpansGraph, Bindings> createGraphBindings = graph -> {
                val bindings = engine.createBindings();
                bindings.put("graph", graph);
                return bindings;
            };
            if (engine instanceof Compilable) {
                val compiledScript = ((Compilable) engine).compile(script);
                graphFunction = graph -> {
                    val bindings = createGraphBindings.apply(graph);
                    compiledScript.eval(bindings);
                };
            } else {
                graphFunction = graph -> {
                    val bindings = createGraphBindings.apply(graph);
                    engine.eval(script, bindings);
                };
            }
        }

        {
            Function<SpecSpanNode, Bindings> createNodeBindings = node -> {
                val bindings = engine.createBindings();
                bindings.put("node", node);
                return bindings;
            };
            if (engine instanceof Compilable) {
                val compiledScript = ((Compilable) engine).compile(script);
                nodeFunction = node -> {
                    val bindings = createNodeBindings.apply(node);
                    compiledScript.eval(bindings);
                };
            } else {
                nodeFunction = node -> {
                    val bindings = createNodeBindings.apply(node);
                    engine.eval(script, bindings);
                };
            }
        }
    }

    @OverrideOnly
    protected void processEngineBindings(Bindings bindings) {
        // can be overridden
    }

    @Override
    public void processGraph(SpecSpansGraph graph) throws Throwable {
        graphFunction.processGraph(graph);
    }

    @Override
    public void processNode(SpecSpanNode node) throws Throwable {
        nodeFunction.processNode(node);
    }


    @FunctionalInterface
    private interface GraphFunction {
        void processGraph(SpecSpansGraph graph) throws Throwable;
    }

    @FunctionalInterface
    private interface NodeFunction {
        void processNode(SpecSpanNode node) throws Throwable;
    }

}
