package name.remal.tracingspec.renderer.processor.script;

import javax.script.Bindings;
import org.intellij.lang.annotations.Language;

public class JsScriptEngineProcessor extends ScriptEngineProcessor {

    public JsScriptEngineProcessor(@Language("JavaScript") String script) {
        super("js", script);
    }

    @Override
    protected void processEngineBindings(Bindings bindings) {
        bindings.put("polyglot.js.nashorn-compat", true);
    }

}
