package name.remal.tracingspec.retriever;

import java.util.List;
import name.remal.tracingspec.model.SpecSpan;

public interface SpecSpansRetriever {

    List<SpecSpan> retrieveSpecSpansForTrace(String traceId);

}
