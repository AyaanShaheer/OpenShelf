package com.openshelf.search.dto;

import com.openshelf.catalogue.dto.ResourceSummaryDto;

import java.util.List;
import java.util.Map;

public record SearchResponse(
        List<ResourceSummaryDto> results,
        long total,
        int page,
        int size,
        Map<String, List<FacetValue>> facets
) {
    public record FacetValue(String value, long count) {}
}
