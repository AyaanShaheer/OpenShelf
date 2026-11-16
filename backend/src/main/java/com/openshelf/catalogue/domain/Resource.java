package com.openshelf.catalogue.domain;

import com.openshelf.author.domain.Author;
import com.openshelf.subject.domain.Subject;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String publisher;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 50)
    private ResourceType resourceType;

    @Column(length = 100)
    private String language;

    @Column(name = "isbn_or_identifier", length = 255)
    private String isbnOrIdentifier;

    @Column(length = 100)
    private String format;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(columnDefinition = "TEXT")
    private String rights;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResourceStatus status = ResourceStatus.ACTIVE;

    @ManyToMany
    @JoinTable(name = "resource_authors",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private Set<Author> authors = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "resource_subjects",
            joinColumns = @JoinColumn(name = "resource_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<Subject> subjects = new LinkedHashSet<>();

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate = publicationDate; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getIsbnOrIdentifier() { return isbnOrIdentifier; }
    public void setIsbnOrIdentifier(String isbnOrIdentifier) { this.isbnOrIdentifier = isbnOrIdentifier; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getRights() { return rights; }
    public void setRights(String rights) { this.rights = rights; }
    public ResourceStatus getStatus() { return status; }
    public void setStatus(ResourceStatus status) { this.status = status; }
    public Set<Author> getAuthors() { return authors; }
    public void setAuthors(Set<Author> authors) { this.authors = authors; }
    public Set<Subject> getSubjects() { return subjects; }
    public void setSubjects(Set<Subject> subjects) { this.subjects = subjects; }
    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
