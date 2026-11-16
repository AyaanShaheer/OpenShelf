package com.openshelf.indexing.service;

import com.openshelf.catalogue.domain.Resource;
import com.openshelf.catalogue.domain.ResourceStatus;
import com.openshelf.catalogue.repository.ResourceRepository;
import com.openshelf.indexing.domain.IndexingState;
import com.openshelf.indexing.domain.IndexingStatus;
import com.openshelf.indexing.repository.IndexingStatusRepository;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolrIndexService {

    private static final Logger log = LoggerFactory.getLogger(SolrIndexService.class);

    private final SolrClient solrClient;
    private final ResourceRepository resourceRepository;
    private final IndexingStatusRepository indexingStatusRepository;

    public SolrIndexService(SolrClient solrClient,
                           ResourceRepository resourceRepository,
                           IndexingStatusRepository indexingStatusRepository) {
        this.solrClient = solrClient;
        this.resourceRepository = resourceRepository;
        this.indexingStatusRepository = indexingStatusRepository;
    }

    @Transactional
    public void indexResource(Long resourceId) {
        Resource resource = resourceRepository.findDetailedById(resourceId)
                .orElse(null);
        if (resource == null) {
            log.warn("Cannot index missing resource id={}", resourceId);
            return;
        }
        IndexingStatus status = indexingStatusRepository.findByResourceId(resourceId)
                .orElseGet(() -> {
                    IndexingStatus s = new IndexingStatus();
                    s.setResource(resource);
                    return s;
                });
        try {
            if (resource.getStatus() == ResourceStatus.ARCHIVED) {
                solrClient.deleteById(String.valueOf(resource.getId()));
            } else {
                solrClient.add(toDocument(resource));
            }
            solrClient.commit();
            status.setStatus(IndexingState.INDEXED);
            status.setLastIndexedAt(Instant.now());
            status.setLastError(null);
        } catch (Exception e) {
            log.error("Failed to index resource id={}", resourceId, e);
            status.setStatus(IndexingState.FAILED);
            status.setLastError(describe(e));
        }
        indexingStatusRepository.save(status);
    }

    @Transactional
    public void removeFromIndex(Long resourceId) {
        IndexingStatus status = indexingStatusRepository.findByResourceId(resourceId).orElse(null);
        try {
            solrClient.deleteById(String.valueOf(resourceId));
            solrClient.commit();
            if (status != null) {
                status.setStatus(IndexingState.INDEXED);
                status.setLastIndexedAt(Instant.now());
                status.setLastError(null);
                indexingStatusRepository.save(status);
            }
        } catch (Exception e) {
            log.error("Failed to remove resource id={} from index", resourceId, e);
            if (status != null) {
                status.setStatus(IndexingState.FAILED);
                status.setLastError(describe(e));
                indexingStatusRepository.save(status);
            }
        }
    }

    @Transactional
    public ReindexResult reindexAll() {
        List<Resource> resources = resourceRepository.findAll();
        int indexed = 0;
        int failed = 0;
        for (Resource resource : resources) {
            IndexingStatus status = indexingStatusRepository.findByResourceId(resource.getId())
                    .orElseGet(() -> {
                        IndexingStatus s = new IndexingStatus();
                        s.setResource(resource);
                        return s;
                    });
            try {
                if (resource.getStatus() == ResourceStatus.ARCHIVED) {
                    solrClient.deleteById(String.valueOf(resource.getId()));
                } else {
                    solrClient.add(toDocument(resource));
                }
                status.setStatus(IndexingState.INDEXED);
                status.setLastIndexedAt(Instant.now());
                status.setLastError(null);
                indexed++;
            } catch (Exception e) {
                log.error("Reindex failed for resource id={}", resource.getId(), e);
                status.setStatus(IndexingState.FAILED);
                status.setLastError(describe(e));
                failed++;
            }
            indexingStatusRepository.save(status);
        }
        try {
            solrClient.commit();
        } catch (Exception e) {
            log.error("Failed to commit Solr after reindex", e);
        }
        return new ReindexResult(indexed, failed);
    }

    private SolrInputDocument toDocument(Resource resource) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", String.valueOf(resource.getId()));
        doc.addField("title", resource.getTitle());
        doc.addField("description", resource.getDescription());
        doc.addField("publisher", resource.getPublisher());
        doc.addField("authors", resource.getAuthors().stream().map(a -> a.getFullName()).collect(Collectors.toList()));
        doc.addField("subjects", resource.getSubjects().stream().map(s -> s.getName()).collect(Collectors.toList()));
        doc.addField("resource_type", resource.getResourceType().name());
        doc.addField("language", resource.getLanguage());
        if (resource.getPublicationDate() != null) {
            doc.addField("publication_year", resource.getPublicationDate().getYear());
            doc.addField("publication_date", Date.from(resource.getPublicationDate().atStartOfDay().toInstant(ZoneOffset.UTC)));
        }
        doc.addField("isbn_or_identifier", resource.getIsbnOrIdentifier());
        doc.addField("status", resource.getStatus().name());
        if (resource.getCreatedAt() != null) doc.addField("created_at", Date.from(resource.getCreatedAt()));
        if (resource.getUpdatedAt() != null) doc.addField("updated_at", Date.from(resource.getUpdatedAt()));
        return doc;
    }

    private String describe(Exception e) {
        String name = e.getClass().getSimpleName();
        String message = e.getMessage();
        if ((message == null || message.isBlank()) && e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        String described = message == null || message.isBlank() ? name : name + ": " + message;
        if (e instanceof java.net.ConnectException) {
            described += " (could not reach Apache Solr; check SOLR_BASE_URL, SOLR_COLLECTION"
                    + " and that the core is running)";
        }
        return described.length() > 2000 ? described.substring(0, 2000) : described;
    }

    public record ReindexResult(int indexed, int failed) {}
}
