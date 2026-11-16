package com.openshelf.imports.domain;

import com.openshelf.auth.domain.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_jobs")
public class ImportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ImportJobStatus status;

    @Column(name = "total_records", nullable = false)
    private int totalRecords;

    @Column(name = "successful_records", nullable = false)
    private int successfulRecords;

    @Column(name = "failed_records", nullable = false)
    private int failedRecords;

    @Column(name = "duplicate_records", nullable = false)
    private int duplicateRecords;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imported_by")
    private User importedBy;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "preview_payload", columnDefinition = "TEXT")
    private String previewPayload;

    @OneToMany(mappedBy = "importJob", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportError> errors = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (startedAt == null) startedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public ImportJobStatus getStatus() { return status; }
    public void setStatus(ImportJobStatus status) { this.status = status; }
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
    public int getSuccessfulRecords() { return successfulRecords; }
    public void setSuccessfulRecords(int successfulRecords) { this.successfulRecords = successfulRecords; }
    public int getFailedRecords() { return failedRecords; }
    public void setFailedRecords(int failedRecords) { this.failedRecords = failedRecords; }
    public int getDuplicateRecords() { return duplicateRecords; }
    public void setDuplicateRecords(int duplicateRecords) { this.duplicateRecords = duplicateRecords; }
    public User getImportedBy() { return importedBy; }
    public void setImportedBy(User importedBy) { this.importedBy = importedBy; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getPreviewPayload() { return previewPayload; }
    public void setPreviewPayload(String previewPayload) { this.previewPayload = previewPayload; }
    public List<ImportError> getErrors() { return errors; }
}
