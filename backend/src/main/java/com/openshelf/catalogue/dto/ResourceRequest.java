package com.openshelf.catalogue.dto;

import com.openshelf.catalogue.domain.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ResourceRequest(
        @NotBlank String title,
        String description,
        String publisher,
        LocalDate publicationDate,
        @NotNull ResourceType resourceType,
        String language,
        String isbnOrIdentifier,
        String format,
        String sourceUrl,
        String rights,
        List<String> authors,
        List<String> subjects
) {}
