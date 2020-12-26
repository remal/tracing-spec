package apps.documents;

import apps.common.repository.Entity;
import apps.documents.ImmutableDocument.DocumentBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = DocumentBuilder.class)
public interface Document extends Entity<DocumentId> {

    @Value.Default
    default ObjectNode getContent() {
        return JsonNodeFactory.instance.objectNode();
    }

}
