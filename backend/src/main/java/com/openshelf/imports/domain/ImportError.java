package com.openshelf.imports.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "import_errors")
public class ImportError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_job_id", nullable = false)
    private ImportJob importJob;

    @Column(name = "record_identifier", length = 255)
    private String recordIdentifier;

    @Column(name = "error_message", nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ImportJob getImportJob() { return importJob; }
    public void setImportJob(ImportJob importJob) { this.importJob = importJob; }
    public String getRecordIdentifier() { return recordIdentifier; }
    public void setRecordIdentifier(String recordIdentifier) { this.recordIdentifier = recordIdentifier; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
