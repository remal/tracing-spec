package apps.documents;

import static apps.documents.StaleDocumentsEvent.STALE_DOCUMENTS_TOPIC;

import lombok.RequiredArgsConstructor;
import name.remal.tracingspec.spring.SpecSpanTags;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaleDocumentsEventSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @NewSpan("send-stale-documents-event")
    @SpecSpanTags(description = "Publish StaleDocumentsEvent")
    public void sendStaleDocumentsEvent(StaleDocumentsEvent event) {
        kafkaTemplate.send(STALE_DOCUMENTS_TOPIC, event);
    }

}
