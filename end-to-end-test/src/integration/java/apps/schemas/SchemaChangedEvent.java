package apps.schemas;

import apps.schemas.ImmutableSchemaChangedEvent.SchemaChangedEventBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = SchemaChangedEventBuilder.class)
public interface SchemaChangedEvent {

    String SCHEMA_CHANGED_TOPIC = "schema-changed";

    String getId();

}
