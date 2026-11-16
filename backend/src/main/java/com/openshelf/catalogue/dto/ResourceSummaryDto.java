package com.openshelf.catalogue.dto;

import java.time.LocalDate;
import java.util.List;

public record ResourceSummaryDto(
        Long id,
        String title,
        String description,
        String publisher,
        LocalDate publicationDate,
        String resourceType,
        String language,
        String isbnOrIdentifier,
        String format,
        String sourceUrl,
        String rights,
        String status,
        List<String> authors,
        List<String> subjects
) {}
