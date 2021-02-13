package apps.documents;

import static apps.schemas.SchemaChangedEvent.SCHEMA_CHANGED_TOPIC;

import apps.schemas.SchemaChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchemaChangedEventListener {

    private final DocumentsReindexer documentsReindexer;

    @KafkaListener(topics = SCHEMA_CHANGED_TOPIC, autoStartup = "true")
    @NewSpan("process-schema-changed-event")
    public void onSchemaChangedEvent(SchemaChangedEvent event) {
        documentsReindexer.reindexDocumentsBySchemaId(event.getId());
    }

}
