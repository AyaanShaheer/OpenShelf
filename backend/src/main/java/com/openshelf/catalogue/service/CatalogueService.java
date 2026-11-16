package com.openshelf.catalogue.service;

import com.openshelf.author.domain.Author;
import com.openshelf.author.repository.AuthorRepository;
import com.openshelf.catalogue.domain.Resource;
import com.openshelf.catalogue.domain.ResourceStatus;
import com.openshelf.catalogue.dto.DashboardStatsDto;
import com.openshelf.catalogue.dto.ResourceDetailDto;
import com.openshelf.catalogue.dto.ResourceRequest;
import com.openshelf.catalogue.dto.ResourceSummaryDto;
import com.openshelf.catalogue.repository.ResourceRepository;
import com.openshelf.common.exception.BadRequestException;
import com.openshelf.common.exception.ConflictException;
import com.openshelf.common.exception.NotFoundException;
import com.openshelf.indexing.domain.IndexingState;
import com.openshelf.indexing.domain.IndexingStatus;
import com.openshelf.indexing.repository.IndexingStatusRepository;
import com.openshelf.indexing.service.SolrIndexService;
import com.openshelf.imports.repository.ImportJobRepository;
import com.openshelf.subject.domain.Subject;
import com.openshelf.subject.repository.SubjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CatalogueService {

    private final ResourceRepository resourceRepository;
    private final AuthorRepository authorRepository;
    private final SubjectRepository subjectRepository;
    private final IndexingStatusRepository indexingStatusRepository;
    private final ImportJobRepository importJobRepository;
    private final SolrIndexService solrIndexService;

    public CatalogueService(ResourceRepository resourceRepository,
                            AuthorRepository authorRepository,
                            SubjectRepository subjectRepository,
                            IndexingStatusRepository indexingStatusRepository,
                            ImportJobRepository importJobRepository,
                            SolrIndexService solrIndexService) {
        this.resourceRepository = resourceRepository;
        this.authorRepository = authorRepository;
        this.subjectRepository = subjectRepository;
        this.indexingStatusRepository = indexingStatusRepository;
        this.importJobRepository = importJobRepository;
        this.solrIndexService = solrIndexService;
    }

    @Transactional(readOnly = true)
    public Page<ResourceSummaryDto> listActive(Pageable pageable) {
        return resourceRepository.findByStatus(ResourceStatus.ACTIVE, pageable).map(this::toSummary);
    }

    @Transactional
    public ResourceDetailDto getPublic(Long id) {
        Resource resource = resourceRepository.findDetailedById(id)
                .filter(r -> r.getStatus() == ResourceStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
        resource.setViewCount(resource.getViewCount() + 1);
        resourceRepository.save(resource);
        return toDetail(resource);
    }

    @Transactional(readOnly = true)
    public ResourceDetailDto getAny(Long id) {
        Resource resource = resourceRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
        return toDetail(resource);
    }

    @Transactional(readOnly = true)
    public List<ResourceSummaryDto> recent() {
        return resourceRepository.findTop10ByStatusOrderByCreatedAtDesc(ResourceStatus.ACTIVE)
                .stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceSummaryDto> popular() {
        return resourceRepository.findTop10ByStatusOrderByViewCountDescCreatedAtDesc(ResourceStatus.ACTIVE)
                .stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filters() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("resourceTypes", Arrays.stream(com.openshelf.catalogue.domain.ResourceType.values()).map(Enum::name).toList());
        out.put("languages", resourceRepository.findAll().stream().map(Resource::getLanguage).filter(StringUtils::hasText).distinct().sorted().toList());
        out.put("subjects", subjectRepository.findAll().stream().map(Subject::getName).distinct().sorted().toList());
        out.put("years", resourceRepository.findAll().stream()
                .map(Resource::getPublicationDate).filter(Objects::nonNull).map(d -> d.getYear()).distinct().sorted(Comparator.reverseOrder()).toList());
        return out;
    }

    @Transactional(readOnly = true)
    public Page<ResourceSummaryDto> adminList(Pageable pageable) {
        return resourceRepository.findAll(pageable).map(this::toSummary);
    }

    @Transactional
    public ResourceDetailDto create(ResourceRequest request) {
        validateIdentifier(request.isbnOrIdentifier(), null);
        Resource resource = new Resource();
        apply(resource, request);
        resource.setStatus(ResourceStatus.ACTIVE);
        Resource saved = resourceRepository.save(resource);
        ensureIndexingStatus(saved, IndexingState.PENDING);
        safeIndex(saved.getId());
        return toDetail(resourceRepository.findDetailedById(saved.getId()).orElse(saved));
    }

    @Transactional
    public ResourceDetailDto update(Long id, ResourceRequest request) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
        validateIdentifier(request.isbnOrIdentifier(), id);
        apply(resource, request);
        Resource saved = resourceRepository.save(resource);
        ensureIndexingStatus(saved, IndexingState.PENDING);
        safeIndex(saved.getId());
        return toDetail(resourceRepository.findDetailedById(saved.getId()).orElse(saved));
    }

    @Transactional
    public void archive(Long id) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
        resource.setStatus(ResourceStatus.ARCHIVED);
        resourceRepository.save(resource);
        ensureIndexingStatus(resource, IndexingState.PENDING);
        safeIndex(id);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDto dashboard() {
        long total = resourceRepository.count();
        long active = resourceRepository.countActive();
        long archived = resourceRepository.countArchived();
        long indexed = indexingStatusRepository.countByStatus(IndexingState.INDEXED);
        long pending = indexingStatusRepository.countByStatus(IndexingState.PENDING);
        long failed = indexingStatusRepository.countByStatus(IndexingState.FAILED);
        long imports = importJobRepository.count();
        return new DashboardStatsDto(total, active, archived, indexed, pending, failed, imports);
    }

    private void apply(Resource resource, ResourceRequest request) {
        resource.setTitle(request.title().trim());
        resource.setDescription(trimToNull(request.description()));
        resource.setPublisher(trimToNull(request.publisher()));
        resource.setPublicationDate(request.publicationDate());
        resource.setResourceType(request.resourceType());
        resource.setLanguage(trimToNull(request.language()));
        resource.setIsbnOrIdentifier(trimToNull(request.isbnOrIdentifier()));
        resource.setFormat(trimToNull(request.format()));
        resource.setSourceUrl(trimToNull(request.sourceUrl()));
        resource.setRights(trimToNull(request.rights()));
        resource.setAuthors(resolveAuthors(request.authors()));
        resource.setSubjects(resolveSubjects(request.subjects()));
    }

    private Set<Author> resolveAuthors(List<String> names) {
        if (names == null) return new LinkedHashSet<>();
        Set<Author> out = new LinkedHashSet<>();
        for (String name : names) {
            if (!StringUtils.hasText(name)) continue;
            String n = name.trim();
            Author author = authorRepository.findByFullNameIgnoreCase(n).orElseGet(() -> {
                Author a = new Author();
                a.setFullName(n);
                return authorRepository.save(a);
            });
            out.add(author);
        }
        return out;
    }

    private Set<Subject> resolveSubjects(List<String> names) {
        if (names == null) return new LinkedHashSet<>();
        Set<Subject> out = new LinkedHashSet<>();
        for (String name : names) {
            if (!StringUtils.hasText(name)) continue;
            String n = name.trim();
            Subject subject = subjectRepository.findByNameIgnoreCase(n).orElseGet(() -> {
                Subject s = new Subject();
                s.setName(n);
                return subjectRepository.save(s);
            });
            out.add(subject);
        }
        return out;
    }

    private void validateIdentifier(String identifier, Long selfId) {
        String idf = trimToNull(identifier);
        if (idf == null) return;
        resourceRepository.findByIsbnOrIdentifier(idf).ifPresent(existing -> {
            if (selfId == null || !existing.getId().equals(selfId)) {
                throw new ConflictException("A resource with this identifier already exists");
            }
        });
    }

    private void ensureIndexingStatus(Resource resource, IndexingState state) {
        IndexingStatus status = indexingStatusRepository.findByResourceId(resource.getId()).orElseGet(() -> {
            IndexingStatus s = new IndexingStatus();
            s.setResource(resource);
            return s;
        });
        status.setStatus(state);
        if (state == IndexingState.PENDING) status.setLastError(null);
        indexingStatusRepository.save(status);
    }

    private void safeIndex(Long resourceId) {
        try {
            solrIndexService.indexResource(resourceId);
        } catch (Exception e) {
            // Indexing failures must not lose authoritative DB records; status is tracked for recovery.
        }
    }

    private String trimToNull(String v) {
        return StringUtils.hasText(v) ? v.trim() : null;
    }

    public ResourceSummaryDto toSummary(Resource r) {
        return new ResourceSummaryDto(
                r.getId(), r.getTitle(), r.getDescription(), r.getPublisher(), r.getPublicationDate(),
                r.getResourceType() != null ? r.getResourceType().name() : null,
                r.getLanguage(), r.getIsbnOrIdentifier(), r.getFormat(), r.getSourceUrl(), r.getRights(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getAuthors() == null ? List.of() : r.getAuthors().stream().map(Author::getFullName).toList(),
                r.getSubjects() == null ? List.of() : r.getSubjects().stream().map(Subject::getName).toList()
        );
    }

    public ResourceDetailDto toDetail(Resource r) {
        return new ResourceDetailDto(
                r.getId(), r.getTitle(), r.getDescription(), r.getPublisher(), r.getPublicationDate(),
                r.getResourceType() != null ? r.getResourceType().name() : null,
                r.getLanguage(), r.getIsbnOrIdentifier(), r.getFormat(), r.getSourceUrl(), r.getRights(),
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getAuthors() == null ? List.of() : r.getAuthors().stream().map(Author::getFullName).toList(),
                r.getSubjects() == null ? List.of() : r.getSubjects().stream().map(Subject::getName).toList(),
                r.getCreatedAt(), r.getUpdatedAt()
        );
    }
}
