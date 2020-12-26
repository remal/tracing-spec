package name.remal.tracingspec.renderer.plantuml;

import static utils.test.normalizer.PumlNormalizer.normalizePuml;

import name.remal.tracingspec.renderer.AbstractStringTracingSpecRenderer;
import name.remal.tracingspec.renderer.AbstractStringTracingSpecRendererTestBase;

public abstract class AbstractPlantumlTracingSpecRendererTestBase<Renderer extends AbstractStringTracingSpecRenderer>
    extends AbstractStringTracingSpecRendererTestBase<Renderer> {

    @Override
    protected final String getExpectedResourceExtension() {
        return "puml";
    }

    @Override
    protected String normalizeResult(String diagram) {
        return normalizePuml(diagram);
    }

}
