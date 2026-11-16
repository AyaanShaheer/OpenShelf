package com.openshelf.catalogue.dto;

public record DashboardStatsDto(
        long totalResources,
        long activeResources,
        long archivedResources,
        long indexedResources,
        long pendingIndexing,
        long failedIndexing,
        long importJobs
) {}
