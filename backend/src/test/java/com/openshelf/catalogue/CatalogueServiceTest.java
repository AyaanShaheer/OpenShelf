package com.openshelf.catalogue;

import com.openshelf.author.repository.AuthorRepository;
import com.openshelf.catalogue.domain.Resource;
import com.openshelf.catalogue.domain.ResourceStatus;
import com.openshelf.catalogue.domain.ResourceType;
import com.openshelf.catalogue.dto.ResourceDetailDto;
import com.openshelf.catalogue.dto.ResourceRequest;
import com.openshelf.catalogue.repository.ResourceRepository;
import com.openshelf.catalogue.service.CatalogueService;
import com.openshelf.common.exception.ConflictException;
import com.openshelf.common.exception.NotFoundException;
import com.openshelf.imports.repository.ImportJobRepository;
import com.openshelf.indexing.domain.IndexingState;
import com.openshelf.indexing.domain.IndexingStatus;
import com.openshelf.indexing.repository.IndexingStatusRepository;
import com.openshelf.indexing.service.SolrIndexService;
import com.openshelf.subject.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogueServiceTest {

    @Mock private ResourceRepository resourceRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private SubjectRepository subjectRepository;
    @Mock private IndexingStatusRepository indexingStatusRepository;
    @Mock private ImportJobRepository importJobRepository;
    @Mock private SolrIndexService solrIndexService;

    @InjectMocks private CatalogueService catalogueService;

    @Test
    void createsResourceAndTracksIndexing() {
        ResourceRequest request = new ResourceRequest(
                "Test Book", "Desc", "Publisher", LocalDate.of(2020, 1, 1),
                ResourceType.BOOK, "English", "ISBN-1", "PDF", "https://example.org/x", "CC BY",
                List.of("Author One"), List.of("Subject One"));

        when(resourceRepository.findByIsbnOrIdentifier("ISBN-1")).thenReturn(Optional.empty());
        when(authorRepository.findByFullNameIgnoreCase("Author One")).thenReturn(Optional.empty());
        when(subjectRepository.findByNameIgnoreCase("Subject One")).thenReturn(Optional.empty());
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> {
            Resource r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });
        when(indexingStatusRepository.findByResourceId(10L)).thenReturn(Optional.empty());
        when(resourceRepository.findDetailedById(10L)).thenAnswer(inv -> {
            Resource r = new Resource();
            r.setId(10L);
            r.setTitle("Test Book");
            r.setResourceType(ResourceType.BOOK);
            r.setStatus(ResourceStatus.ACTIVE);
            return Optional.of(r);
        });

        ResourceDetailDto dto = catalogueService.create(request);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.title()).isEqualTo("Test Book");
        verify(indexingStatusRepository, atLeastOnce()).save(any(IndexingStatus.class));
        verify(solrIndexService).indexResource(10L);
    }

    @Test
    void rejectsDuplicateIdentifierOnCreate() {
        Resource existing = new Resource();
        existing.setId(1L);
        when(resourceRepository.findByIsbnOrIdentifier("ISBN-1")).thenReturn(Optional.of(existing));
        ResourceRequest request = new ResourceRequest(
                "Duplicate", null, null, null, ResourceType.BOOK, null,
                "ISBN-1", null, null, null, List.of(), List.of());

        assertThatThrownBy(() -> catalogueService.create(request)).isInstanceOf(ConflictException.class);
        verify(resourceRepository, never()).save(any());
    }

    @Test
    void publicDetailOnlyReturnsActive() {
        Resource archived = new Resource();
        archived.setId(5L);
        archived.setStatus(ResourceStatus.ARCHIVED);
        when(resourceRepository.findDetailedById(5L)).thenReturn(Optional.of(archived));

        assertThatThrownBy(() -> catalogueService.getPublic(5L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void publicDetailIncrementsViewCount() {
        Resource active = new Resource();
        active.setId(7L);
        active.setTitle("Viewed Resource");
        active.setResourceType(ResourceType.BOOK);
        active.setStatus(ResourceStatus.ACTIVE);
        active.setViewCount(3);
        when(resourceRepository.findDetailedById(7L)).thenReturn(Optional.of(active));
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> inv.getArgument(0));

        ResourceDetailDto dto = catalogueService.getPublic(7L);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(active.getViewCount()).isEqualTo(4);
        verify(resourceRepository).save(active);
    }

    @Test
    void indexingFailureDoesNotBreakCreate() {
        ResourceRequest request = new ResourceRequest(
                "Book", null, null, null, ResourceType.BOOK, null, "ISBN-2", null, null, null, List.of(), List.of());
        when(resourceRepository.findByIsbnOrIdentifier("ISBN-2")).thenReturn(Optional.empty());
        when(resourceRepository.save(any(Resource.class))).thenAnswer(inv -> {
            Resource r = inv.getArgument(0);
            r.setId(11L);
            return r;
        });
        when(indexingStatusRepository.findByResourceId(11L)).thenReturn(Optional.empty());
        when(resourceRepository.findDetailedById(11L)).thenAnswer(inv -> {
            Resource r = new Resource();
            r.setId(11L);
            r.setTitle("Book");
            r.setResourceType(ResourceType.BOOK);
            r.setStatus(ResourceStatus.ACTIVE);
            return Optional.of(r);
        });
        doThrow(new RuntimeException("Solr down")).when(solrIndexService).indexResource(11L);

        ResourceDetailDto dto = catalogueService.create(request);
        assertThat(dto.id()).isEqualTo(11L);
        verify(solrIndexService).indexResource(11L);
    }
}
