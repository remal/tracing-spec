package apps.documents;

import static apps.documents.StaleDocumentsEvent.STALE_DOCUMENTS_TOPIC;
import static java.util.stream.Collectors.toList;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaleDocumentsEventListener {

    private final DocumentRepository repository;

    private final DocumentsReindexer documentsReindexer;

    @KafkaListener(topics = STALE_DOCUMENTS_TOPIC, autoStartup = "true")
    @NewSpan("process-stale-documents-event")
    public void onDocumentsShouldBeUpdatedEvent(StaleDocumentsEvent event) {
        val docs = event.getIds().stream()
            .distinct()
            .map(repository::getById)
            .collect(toList());
        documentsReindexer.reindexDocuments(docs);
    }

}
