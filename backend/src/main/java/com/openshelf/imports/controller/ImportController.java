package com.openshelf.imports.controller;

import com.openshelf.common.api.ApiResponse;
import com.openshelf.imports.dto.ImportDtos;
import com.openshelf.imports.service.ImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/imports")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImportDtos.PreviewResponse> preview(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(importService.preview(file), "Import preview generated");
    }

    @PostMapping("/{id}/commit")
    public ApiResponse<ImportDtos.CommitResponse> commit(@PathVariable Long id) {
        return ApiResponse.ok(importService.commit(id), "Import commit processed");
    }

    @GetMapping
    public ApiResponse<List<ImportDtos.ImportJobDto>> history() {
        return ApiResponse.ok(importService.history());
    }

    @GetMapping("/{id}")
    public ApiResponse<ImportDtos.ImportJobDto> get(@PathVariable Long id) {
        return ApiResponse.ok(importService.get(id));
    }
}
