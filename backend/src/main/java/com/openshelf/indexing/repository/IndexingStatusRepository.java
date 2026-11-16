package com.openshelf.indexing.repository;

import com.openshelf.indexing.domain.IndexingState;
import com.openshelf.indexing.domain.IndexingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IndexingStatusRepository extends JpaRepository<IndexingStatus, Long> {
    Optional<IndexingStatus> findByResourceId(Long resourceId);
    long countByStatus(IndexingState status);
    List<IndexingStatus> findByStatus(IndexingState status);
}
