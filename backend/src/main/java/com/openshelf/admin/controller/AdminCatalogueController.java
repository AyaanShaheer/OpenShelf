package com.openshelf.admin.controller;

import com.openshelf.catalogue.dto.DashboardStatsDto;
import com.openshelf.catalogue.dto.ResourceDetailDto;
import com.openshelf.catalogue.dto.ResourceRequest;
import com.openshelf.catalogue.dto.ResourceSummaryDto;
import com.openshelf.catalogue.service.CatalogueService;
import com.openshelf.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCatalogueController {

    private final CatalogueService catalogueService;

    public AdminCatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<DashboardStatsDto> dashboard() {
        return ApiResponse.ok(catalogueService.dashboard());
    }

    @GetMapping("/resources")
    public ApiResponse<Page<ResourceSummaryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.ok(catalogueService.adminList(pageable));
    }

    @GetMapping("/resources/{id}")
    public ApiResponse<ResourceDetailDto> get(@PathVariable Long id) {
        return ApiResponse.ok(catalogueService.getAny(id));
    }

    @PostMapping("/resources")
    public ApiResponse<ResourceDetailDto> create(@Valid @RequestBody ResourceRequest request) {
        return ApiResponse.ok(catalogueService.create(request), "Resource created");
    }

    @PutMapping("/resources/{id}")
    public ApiResponse<ResourceDetailDto> update(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        return ApiResponse.ok(catalogueService.update(id, request), "Resource updated");
    }

    @DeleteMapping("/resources/{id}")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        catalogueService.archive(id);
        return ApiResponse.ok(null, "Resource archived");
    }
}
