package apps.schemas;

import apps.common.repository.AbstractInMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("java:S1171")
public class SchemaRepository extends AbstractInMemoryRepository<String, Schema> {

    private final SchemaChangedEventSender schemaChangedEventSender;

    @Override
    protected void onEntityChanged(Schema entity) {
        schemaChangedEventSender.sendSchemaChangedEvent(ImmutableSchemaChangedEvent.builder()
            .id(entity.getId())
            .build()
        );
    }

}
