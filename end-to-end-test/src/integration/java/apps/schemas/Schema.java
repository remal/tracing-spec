package apps.schemas;

import apps.common.repository.Entity;
import apps.schemas.ImmutableSchema.SchemaBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = SchemaBuilder.class)
public interface Schema extends Entity<String> {

    List<SchemaReference> getReferences();

}
