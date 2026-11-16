package com.openshelf.imports.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openshelf.auth.domain.User;
import com.openshelf.auth.repository.UserRepository;
import com.openshelf.catalogue.domain.Resource;
import com.openshelf.catalogue.domain.ResourceStatus;
import com.openshelf.catalogue.domain.ResourceType;
import com.openshelf.catalogue.repository.ResourceRepository;
import com.openshelf.common.exception.BadRequestException;
import com.openshelf.common.exception.NotFoundException;
import com.openshelf.imports.domain.ImportError;
import com.openshelf.imports.domain.ImportJob;
import com.openshelf.imports.domain.ImportJobStatus;
import com.openshelf.imports.dto.ImportDtos;
import com.openshelf.metadata.DublinCoreXmlParser;
import com.openshelf.imports.repository.ImportJobRepository;
import com.openshelf.indexing.domain.IndexingState;
import com.openshelf.indexing.domain.IndexingStatus;
import com.openshelf.indexing.repository.IndexingStatusRepository;
import com.openshelf.indexing.service.SolrIndexService;
import com.openshelf.author.domain.Author;
import com.openshelf.author.repository.AuthorRepository;
import com.openshelf.subject.domain.Subject;
import com.openshelf.subject.repository.SubjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ImportService {

    private final DublinCoreXmlParser parser;
    private final ImportJobRepository importJobRepository;
    private final ResourceRepository resourceRepository;
    private final AuthorRepository authorRepository;
    private final SubjectRepository subjectRepository;
    private final IndexingStatusRepository indexingStatusRepository;
    private final SolrIndexService solrIndexService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public ImportService(DublinCoreXmlParser parser,
                         ImportJobRepository importJobRepository,
                         ResourceRepository resourceRepository,
                         AuthorRepository authorRepository,
                         SubjectRepository subjectRepository,
                         IndexingStatusRepository indexingStatusRepository,
                         SolrIndexService solrIndexService,
                         UserRepository userRepository,
                         ObjectMapper objectMapper) {
        this.parser = parser;
        this.importJobRepository = importJobRepository;
        this.resourceRepository = resourceRepository;
        this.authorRepository = authorRepository;
        this.subjectRepository = subjectRepository;
        this.indexingStatusRepository = indexingStatusRepository;
        this.solrIndexService = solrIndexService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ImportDtos.PreviewResponse preview(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("XML file is required");
        String filename = file.getOriginalFilename() == null ? "upload.xml" : file.getOriginalFilename();
        List<Map<String, String>> raw;
        try {
            raw = parser.parse(file.getInputStream());
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not read uploaded XML file");
        }
        if (raw.isEmpty()) throw new BadRequestException("No metadata records found in the XML file");

        Set<String> seenIdentifiers = new HashSet<>();
        List<ImportDtos.PreviewRecord> records = new ArrayList<>();
        List<Map<String, String>> validPayload = new ArrayList<>();
        int valid = 0, invalid = 0, duplicates = 0;

        for (Map<String, String> m : raw) {
            String identifier = norm(m.get("identifier"));
            String title = norm(m.get("title"));
            List<String> errors = new ArrayList<>();
            boolean duplicate = false;

            if (!StringUtils.hasText(title)) errors.add("Missing mandatory field: dc:title");
            if (!StringUtils.hasText(identifier)) errors.add("Missing mandatory field: dc:identifier");

            if (StringUtils.hasText(identifier)) {
                String key = identifier.toLowerCase(Locale.ROOT);
                if (!seenIdentifiers.add(key)) {
                    duplicate = true;
                    errors.add("Duplicate identifier within uploaded file");
                } else if (resourceRepository.findByIsbnOrIdentifier(identifier).isPresent()) {
                    duplicate = true;
                    errors.add("Duplicate identifier already exists in catalogue");
                }
            }

            boolean isValid = errors.isEmpty();
            if (isValid) {
                valid++;
                validPayload.add(new LinkedHashMap<>(m));
            } else if (duplicate) {
                duplicates++;
            } else {
                invalid++;
            }

            records.add(new ImportDtos.PreviewRecord(
                    identifier, title, norm(m.get("creator")), norm(m.get("subject")), norm(m.get("description")),
                    norm(m.get("publisher")), norm(m.get("date")), norm(m.get("type")), norm(m.get("format")),
                    norm(m.get("source")), norm(m.get("language")), norm(m.get("rights")), errors, duplicate
            ));
        }

        ImportJob job = new ImportJob();
        job.setFilename(filename);
        job.setStatus(ImportJobStatus.PREVIEW);
        job.setTotalRecords(raw.size());
        job.setSuccessfulRecords(0);
        job.setFailedRecords(invalid);
        job.setDuplicateRecords(duplicates);
        job.setImportedBy(currentUser());
        job.setStartedAt(Instant.now());
        try {
            job.setPreviewPayload(objectMapper.writeValueAsString(validPayload));
        } catch (Exception e) {
            throw new BadRequestException("Failed to prepare import preview");
        }

        // store record-level errors for history visibility
        for (ImportDtos.PreviewRecord r : records) {
            for (String err : r.errors()) {
                ImportError ie = new ImportError();
                ie.setImportJob(job);
                ie.setRecordIdentifier(r.identifier());
                ie.setErrorMessage(err);
                job.getErrors().add(ie);
            }
        }
        ImportJob saved = importJobRepository.save(job);
        return new ImportDtos.PreviewResponse(saved.getId(), filename, raw.size(), valid, invalid, duplicates, records);
    }

    @Transactional
    public ImportDtos.CommitResponse commit(Long id) {
        ImportJob job = importJobRepository.findById(id).orElseThrow(() -> new NotFoundException("Import job not found"));
        if (job.getStatus() == ImportJobStatus.COMMITTED || job.getStatus() == ImportJobStatus.PARTIAL) {
            return new ImportDtos.CommitResponse(job.getId(), job.getStatus().name(), job.getSuccessfulRecords(), job.getFailedRecords(), job.getDuplicateRecords());
        }
        if (job.getStatus() != ImportJobStatus.PREVIEW) {
            throw new BadRequestException("Import job cannot be committed in its current state");
        }
        List<Map<String, String>> payload;
        try {
            payload = objectMapper.readValue(job.getPreviewPayload(), new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            job.setStatus(ImportJobStatus.FAILED);
            job.setCompletedAt(Instant.now());
            importJobRepository.save(job);
            throw new BadRequestException("Import preview data is invalid or unavailable");
        }

        int success = 0;
        int commitFailures = 0;
        for (Map<String, String> m : payload) {
            String identifier = norm(m.get("identifier"));
            if (StringUtils.hasText(identifier) && resourceRepository.findByIsbnOrIdentifier(identifier).isPresent()) {
                // duplicate at commit time (race/changed state)
                continue;
            }
            try {
                Resource resource = mapToResource(m);
                Resource saved = resourceRepository.save(resource);
                IndexingStatus status = new IndexingStatus();
                status.setResource(saved);
                status.setStatus(IndexingState.PENDING);
                indexingStatusRepository.save(status);
                try { solrIndexService.indexResource(saved.getId()); } catch (Exception ignored) {}
                success++;
            } catch (Exception e) {
                commitFailures++;
                ImportError ie = new ImportError();
                ie.setImportJob(job);
                ie.setRecordIdentifier(identifier);
                ie.setErrorMessage("Failed to persist record during commit");
                job.getErrors().add(ie);
            }
        }

        job.setSuccessfulRecords(success);
        job.setFailedRecords(job.getFailedRecords() + commitFailures);
        if (success == 0 && (job.getFailedRecords() > 0 || job.getDuplicateRecords() > 0)) {
            job.setStatus(ImportJobStatus.FAILED);
        } else if (commitFailures > 0 || job.getFailedRecords() > 0 || job.getDuplicateRecords() > 0) {
            job.setStatus(success > 0 ? ImportJobStatus.PARTIAL : ImportJobStatus.FAILED);
        } else {
            job.setStatus(ImportJobStatus.COMMITTED);
        }
        job.setCompletedAt(Instant.now());
        // clear payload after commit to avoid retaining unnecessary data
        job.setPreviewPayload(null);
        importJobRepository.save(job);
        return new ImportDtos.CommitResponse(job.getId(), job.getStatus().name(), job.getSuccessfulRecords(), job.getFailedRecords(), job.getDuplicateRecords());
    }

    @Transactional(readOnly = true)
    public List<ImportDtos.ImportJobDto> history() {
        return importJobRepository.findAll().stream()
                .sorted(Comparator.comparing(ImportJob::getStartedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ImportDtos.ImportJobDto get(Long id) {
        ImportJob job = importJobRepository.findDetailedById(id).orElseThrow(() -> new NotFoundException("Import job not found"));
        return toDto(job);
    }

    private Resource mapToResource(Map<String, String> m) {
        Resource r = new Resource();
        r.setTitle(norm(m.get("title")));
        r.setDescription(norm(m.get("description")));
        r.setPublisher(norm(m.get("publisher")));
        r.setPublicationDate(parseDate(norm(m.get("date"))));
        r.setResourceType(mapType(norm(m.get("type"))));
        r.setLanguage(norm(m.get("language")));
        r.setIsbnOrIdentifier(norm(m.get("identifier")));
        r.setFormat(norm(m.get("format")));
        r.setSourceUrl(norm(m.get("source")));
        r.setRights(norm(m.get("rights")));
        r.setStatus(ResourceStatus.ACTIVE);
        r.setAuthors(resolveAuthors(norm(m.get("creator"))));
        r.setSubjects(resolveSubjects(norm(m.get("subject"))));
        return r;
    }

    private Set<Author> resolveAuthors(String creator) {
        if (!StringUtils.hasText(creator)) return new LinkedHashSet<>();
        Set<Author> out = new LinkedHashSet<>();
        for (String name : creator.split("[;,]")) {
            String n = name.trim();
            if (n.isEmpty()) continue;
            Author a = authorRepository.findByFullNameIgnoreCase(n).orElseGet(() -> {
                Author na = new Author();
                na.setFullName(n);
                return authorRepository.save(na);
            });
            out.add(a);
        }
        return out;
    }

    private Set<Subject> resolveSubjects(String subject) {
        if (!StringUtils.hasText(subject)) return new LinkedHashSet<>();
        Set<Subject> out = new LinkedHashSet<>();
        for (String name : subject.split("[;,]")) {
            String n = name.trim();
            if (n.isEmpty()) continue;
            Subject s = subjectRepository.findByNameIgnoreCase(n).orElseGet(() -> {
                Subject ns = new Subject();
                ns.setName(n);
                return subjectRepository.save(ns);
            });
            out.add(s);
        }
        return out;
    }

    private ResourceType mapType(String type) {
        if (!StringUtils.hasText(type)) return ResourceType.OTHER;
        String t = type.trim().toLowerCase(Locale.ROOT);
        if (t.contains("book")) return ResourceType.BOOK;
        if (t.contains("journal")) return ResourceType.JOURNAL;
        if (t.contains("thesis")) return ResourceType.THESIS;
        if (t.contains("research") || t.contains("paper")) return ResourceType.RESEARCH_PAPER;
        if (t.contains("article")) return ResourceType.ARTICLE;
        try { return ResourceType.valueOf(type.trim().toUpperCase(Locale.ROOT).replace(' ', '_')); } catch (Exception ignored) {}
        return ResourceType.OTHER;
    }

    private LocalDate parseDate(String date) {
        if (!StringUtils.hasText(date)) return null;
        String d = date.trim();
        try {
            if (d.length() == 4) return LocalDate.of(Integer.parseInt(d), 1, 1);
            if (d.length() >= 10) return LocalDate.parse(d.substring(0, 10));
            if (d.length() >= 7) return LocalDate.parse(d.substring(0, 7) + "-01");
        } catch (Exception ignored) {}
        return null;
    }

    private String norm(String v) { return StringUtils.hasText(v) ? v.trim() : null; }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return userRepository.findByEmailIgnoreCase(auth.getName()).orElse(null);
    }

    private ImportDtos.ImportJobDto toDto(ImportJob job) {
        List<ImportDtos.ImportErrorDto> errs = job.getErrors() == null ? List.of() :
                job.getErrors().stream().map(e -> new ImportDtos.ImportErrorDto(e.getRecordIdentifier(), e.getErrorMessage())).toList();
        String by = job.getImportedBy() != null ? job.getImportedBy().getEmail() : null;
        return new ImportDtos.ImportJobDto(job.getId(), job.getFilename(), job.getStatus().name(),
                job.getTotalRecords(), job.getSuccessfulRecords(), job.getFailedRecords(), job.getDuplicateRecords(),
                by, job.getStartedAt(), job.getCompletedAt(), errs);
    }
}
