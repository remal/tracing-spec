package name.remal.tracingspec.application;

import name.remal.tracingspec.renderer.TracingSpecRenderer;

interface RendersRegistry {

    TracingSpecRenderer<?> getRendererByName(String rendererName);

}
