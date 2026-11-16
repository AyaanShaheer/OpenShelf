package com.openshelf.indexing.domain;

import com.openshelf.catalogue.domain.Resource;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "indexing_status")
public class IndexingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false, unique = true)
    private Resource resource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IndexingState status;

    @Column(name = "last_indexed_at")
    private Instant lastIndexedAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }
    public IndexingState getStatus() { return status; }
    public void setStatus(IndexingState status) { this.status = status; }
    public Instant getLastIndexedAt() { return lastIndexedAt; }
    public void setLastIndexedAt(Instant lastIndexedAt) { this.lastIndexedAt = lastIndexedAt; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
