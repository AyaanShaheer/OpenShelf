package com.openshelf.indexing.controller;

import com.openshelf.common.api.ApiResponse;
import com.openshelf.indexing.domain.IndexingState;
import com.openshelf.indexing.domain.IndexingStatus;
import com.openshelf.indexing.repository.IndexingStatusRepository;
import com.openshelf.indexing.service.SolrIndexService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/indexing")
public class IndexingController {

    private final IndexingStatusRepository indexingStatusRepository;
    private final SolrIndexService solrIndexService;

    public IndexingController(IndexingStatusRepository indexingStatusRepository, SolrIndexService solrIndexService) {
        this.indexingStatusRepository = indexingStatusRepository;
        this.solrIndexService = solrIndexService;
    }

    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indexed", indexingStatusRepository.countByStatus(IndexingState.INDEXED));
        out.put("pending", indexingStatusRepository.countByStatus(IndexingState.PENDING));
        out.put("failed", indexingStatusRepository.countByStatus(IndexingState.FAILED));
        List<Map<String, Object>> failures = indexingStatusRepository.findByStatus(IndexingState.FAILED).stream()
                .limit(50)
                .map(this::toFailure)
                .toList();
        out.put("failures", failures);
        return ApiResponse.ok(out);
    }

    @PostMapping("/reindex")
    public ApiResponse<Map<String, Object>> reindex() {
        SolrIndexService.ReindexResult result = solrIndexService.reindexAll();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("indexed", result.indexed());
        out.put("failed", result.failed());
        return ApiResponse.ok(out, "Reindex completed");
    }

    private Map<String, Object> toFailure(IndexingStatus s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("resourceId", s.getResource() != null ? s.getResource().getId() : null);
        m.put("lastIndexedAt", s.getLastIndexedAt());
        m.put("lastError", s.getLastError());
        return m;
    }
}
