package com.openshelf.catalogue.repository;

import com.openshelf.catalogue.domain.Resource;
import com.openshelf.catalogue.domain.ResourceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Query("select r from Resource r where r.isbnOrIdentifier = :isbnOrIdentifier")
    Optional<Resource> findByIsbnOrIdentifier(@org.springframework.data.repository.query.Param("isbnOrIdentifier") String isbnOrIdentifier);

    @EntityGraph(attributePaths = {"authors", "subjects"})
    Optional<Resource> findDetailedById(Long id);

    Page<Resource> findByStatus(ResourceStatus status, Pageable pageable);

    @Query("select count(r) from Resource r where r.status = com.openshelf.catalogue.domain.ResourceStatus.ACTIVE")
    long countActive();

    @Query("select count(r) from Resource r where r.status = com.openshelf.catalogue.domain.ResourceStatus.ARCHIVED")
    long countArchived();

    @EntityGraph(attributePaths = {"authors", "subjects"})
    List<Resource> findTop10ByStatusOrderByCreatedAtDesc(ResourceStatus status);

    @EntityGraph(attributePaths = {"authors", "subjects"})
    List<Resource> findTop10ByStatusOrderByViewCountDescCreatedAtDesc(ResourceStatus status);
}
