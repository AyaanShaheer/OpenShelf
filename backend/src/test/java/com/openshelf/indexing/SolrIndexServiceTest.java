package com.openshelf.indexing;

import com.openshelf.catalogue.domain.Resource;
import com.openshelf.catalogue.domain.ResourceStatus;
import com.openshelf.catalogue.domain.ResourceType;
import com.openshelf.catalogue.repository.ResourceRepository;
import com.openshelf.indexing.domain.IndexingState;
import com.openshelf.indexing.domain.IndexingStatus;
import com.openshelf.indexing.repository.IndexingStatusRepository;
import com.openshelf.indexing.service.SolrIndexService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.ConnectException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolrIndexServiceTest {

    @Mock private SolrClient solrClient;
    @Mock private ResourceRepository resourceRepository;
    @Mock private IndexingStatusRepository indexingStatusRepository;

    private SolrIndexService service;

    @BeforeEach
    void setUp() {
        service = new SolrIndexService(solrClient, resourceRepository, indexingStatusRepository);
    }

    @Test
    void recordsActionableErrorWhenSolrIsUnreachable() throws Exception {
        Resource resource = new Resource();
        resource.setId(7L);
        resource.setTitle("Unreachable");
        resource.setResourceType(ResourceType.BOOK);
        resource.setStatus(ResourceStatus.ACTIVE);

        when(resourceRepository.findDetailedById(7L)).thenReturn(Optional.of(resource));
        when(indexingStatusRepository.findByResourceId(7L)).thenReturn(Optional.empty());
        when(solrClient.add(any(SolrInputDocument.class))).thenThrow(new ConnectException());

        service.indexResource(7L);

        ArgumentCaptor<IndexingStatus> captor = ArgumentCaptor.forClass(IndexingStatus.class);
        verify(indexingStatusRepository).save(captor.capture());
        IndexingStatus saved = captor.getValue();

        assertThat(saved.getStatus()).isEqualTo(IndexingState.FAILED);
        assertThat(saved.getLastError())
                .contains("ConnectException")
                .contains("could not reach Apache Solr")
                .contains("SOLR_BASE_URL");
    }
}
