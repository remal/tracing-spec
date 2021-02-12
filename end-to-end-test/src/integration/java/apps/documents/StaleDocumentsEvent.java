package apps.documents;

import apps.documents.ImmutableStaleDocumentsEvent.StaleDocumentsEventBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import org.immutables.value.Value;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@Value.Immutable
@JsonDeserialize(builder = StaleDocumentsEventBuilder.class)
public interface StaleDocumentsEvent {

    String STALE_DOCUMENTS_TOPIC = "stale-documents";

    List<DocumentId> getIds();


    @Value.Check
    @OverrideOnly
    default void validate() {
        if (getIds().isEmpty()) {
            throw new IllegalStateException("ids must not be empty");
        }
    }

}
