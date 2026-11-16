package com.openshelf.catalogue.controller;

import com.openshelf.catalogue.dto.ResourceDetailDto;
import com.openshelf.catalogue.dto.ResourceSummaryDto;
import com.openshelf.catalogue.service.CatalogueService;
import com.openshelf.common.api.ApiResponse;
import com.openshelf.search.dto.SearchRequest;
import com.openshelf.search.dto.SearchResponse;
import com.openshelf.search.service.SolrSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/catalogue")
public class PublicCatalogueController {

    private final CatalogueService catalogueService;
    private final SolrSearchService solrSearchService;

    public PublicCatalogueController(CatalogueService catalogueService, SolrSearchService solrSearchService) {
        this.catalogueService = catalogueService;
        this.solrSearchService = solrSearchService;
    }

    @GetMapping
    public ApiResponse<Page<ResourceSummaryDto>> browse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), parseSort(sort));
        return ApiResponse.ok(catalogueService.listActive(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<ResourceDetailDto> detail(@PathVariable Long id) {
        return ApiResponse.ok(catalogueService.getPublic(id));
    }

    @GetMapping("/search")
    public ApiResponse<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) List<String> languages,
            @RequestParam(required = false) List<String> subjects,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        SearchRequest request = new SearchRequest(q, types, languages, subjects, yearFrom, yearTo, sort, page, size);
        return ApiResponse.ok(solrSearchService.search(request));
    }

    @GetMapping("/filters")
    public ApiResponse<Map<String, Object>> filters() {
        return ApiResponse.ok(catalogueService.filters());
    }

    @GetMapping("/recent")
    public ApiResponse<List<ResourceSummaryDto>> recent() {
        return ApiResponse.ok(catalogueService.recent());
    }

    @GetMapping("/popular")
    public ApiResponse<List<ResourceSummaryDto>> popular() {
        return ApiResponse.ok(catalogueService.popular());
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) return Sort.by(Sort.Direction.DESC, "createdAt");
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(dir, field);
    }
}
