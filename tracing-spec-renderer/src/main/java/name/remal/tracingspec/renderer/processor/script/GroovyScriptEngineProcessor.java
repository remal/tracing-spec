package name.remal.tracingspec.renderer.processor.script;

import org.intellij.lang.annotations.Language;

public class GroovyScriptEngineProcessor extends ScriptEngineProcessor {

    public GroovyScriptEngineProcessor(@Language("Groovy") String script) {
        super("groovy", script);
    }

}
