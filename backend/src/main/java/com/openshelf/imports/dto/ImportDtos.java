package com.openshelf.imports.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ImportDtos {
    public record PreviewRecord(
            String identifier,
            String title,
            String creator,
            String subject,
            String description,
            String publisher,
            String date,
            String type,
            String format,
            String source,
            String language,
            String rights,
            List<String> errors,
            boolean duplicate
    ) {}

    public record PreviewResponse(
            Long importJobId,
            String filename,
            int totalRecords,
            int validRecords,
            int invalidRecords,
            int duplicateRecords,
            List<PreviewRecord> records
    ) {}

    public record ImportErrorDto(String recordIdentifier, String errorMessage) {}

    public record ImportJobDto(
            Long id,
            String filename,
            String status,
            int totalRecords,
            int successfulRecords,
            int failedRecords,
            int duplicateRecords,
            String importedBy,
            Instant startedAt,
            Instant completedAt,
            List<ImportErrorDto> errors
    ) {}

    public record CommitResponse(Long importJobId, String status, int successfulRecords, int failedRecords, int duplicateRecords) {}
}
