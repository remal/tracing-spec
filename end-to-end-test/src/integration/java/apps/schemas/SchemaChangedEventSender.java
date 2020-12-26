package apps.schemas;

import static apps.schemas.SchemaChangedEvent.SCHEMA_CHANGED_TOPIC;

import lombok.RequiredArgsConstructor;
import name.remal.tracingspec.spring.SpecSpanTags;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchemaChangedEventSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @NewSpan("send-schema-changed-event")
    @SpecSpanTags(description = "Publish SchemaChangedEvent")
    public void sendSchemaChangedEvent(SchemaChangedEvent event) {
        kafkaTemplate.send(SCHEMA_CHANGED_TOPIC, event);
    }

}
