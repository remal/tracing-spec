package apps.schemas;

import apps.schemas.ImmutableSchemaReference.SchemaReferenceBuilder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashSet;
import java.util.Map;
import lombok.val;
import org.immutables.value.Value;

@Value.Immutable
@JsonDeserialize(builder = SchemaReferenceBuilder.class)
public interface SchemaReference {

    String getDataType();

    String getIdField();

    Map<String, String> getFieldMappings();


    @Value.Check
    default void validate() {
        val targetFields = getFieldMappings().values();
        if (targetFields.contains(getIdField())) {
            throw new IllegalStateException("fieldMappings targets contain idField: " + getIdField());
        }
        if (new HashSet<>(targetFields).size() != targetFields.size()) {
            throw new IllegalStateException("fieldMappings targets are not unique");
        }
    }

}
