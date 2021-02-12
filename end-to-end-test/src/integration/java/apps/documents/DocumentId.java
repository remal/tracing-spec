package apps.documents;

import apps.documents.ImmutableDocumentId.DocumentIdBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = DocumentIdBuilder.class)
public interface DocumentId {

    String getSchema();

    long getKey();

}
