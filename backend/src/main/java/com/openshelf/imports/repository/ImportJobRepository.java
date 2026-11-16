package com.openshelf.imports.repository;

import com.openshelf.imports.domain.ImportJob;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    @EntityGraph(attributePaths = {"errors", "importedBy"})
    Optional<ImportJob> findDetailedById(Long id);
}
