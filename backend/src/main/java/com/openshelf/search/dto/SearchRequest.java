package com.openshelf.search.dto;

import java.util.List;

public record SearchRequest(
        String q,
        List<String> types,
        List<String> languages,
        List<String> subjects,
        Integer yearFrom,
        Integer yearTo,
        String sort,
        int page,
        int size
) {}
