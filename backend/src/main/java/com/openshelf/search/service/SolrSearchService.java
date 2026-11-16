package com.openshelf.search.service;

import com.openshelf.catalogue.dto.ResourceSummaryDto;
import com.openshelf.common.exception.ServiceUnavailableException;
import com.openshelf.search.dto.SearchRequest;
import com.openshelf.search.dto.SearchResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class SolrSearchService {

    private static final Logger log = LoggerFactory.getLogger(SolrSearchService.class);

    private final SolrClient solrClient;

    public SolrSearchService(SolrClient solrClient) {
        this.solrClient = solrClient;
    }

    public SearchResponse search(SearchRequest request) {
        try {
            int page = Math.max(request.page(), 0);
            int size = Math.min(Math.max(request.size(), 1), 50);
            SolrQuery query = new SolrQuery();
            query.setQuery(StringUtils.hasText(request.q())
                    ? sanitizeQuery(request.q())
                    : "*:*");
            query.set("defType", "edismax");
            query.set("qf", "title^4 authors^3 subjects^2 publisher^2 description isbn_or_identifier");
            query.setStart(page * size);
            query.setRows(size);
            query.addFilterQuery("status:ACTIVE");

            addOrFilter(query, "resource_type", request.types());
            addOrFilter(query, "language", request.languages());
            addOrFilter(query, "subject_facet", request.subjects());

            if (request.yearFrom() != null || request.yearTo() != null) {
                String from = request.yearFrom() != null ? String.valueOf(request.yearFrom()) : "*";
                String to = request.yearTo() != null ? String.valueOf(request.yearTo()) : "*";
                query.addFilterQuery("publication_year:[" + from + " TO " + to + "]");
            }

            applySort(query, request.sort(), StringUtils.hasText(request.q()));

            query.setFacet(true);
            query.addFacetField("resource_type", "language", "subject_facet");
            query.setFacetMinCount(1);
            query.setFacetLimit(20);

            QueryResponse response = solrClient.query(query);
            SolrDocumentList docs = response.getResults();
            List<ResourceSummaryDto> results = docs.stream().map(this::toSummary).toList();
            Map<String, List<SearchResponse.FacetValue>> facets = extractFacets(response);
            List<SearchResponse.FacetValue> subjectFacets = facets.remove("subject_facet");
            if (subjectFacets != null) facets.put("subjects", subjectFacets);
            return new SearchResponse(results, docs.getNumFound(), page, size, facets);
        } catch (Exception e) {
            log.error("Solr search failed", e);
            throw new ServiceUnavailableException("Search service is temporarily unavailable", e);
        }
    }

    /**
     * Escapes each whitespace-separated term individually so user input cannot inject
     * Solr query syntax while multi-word searches keep working.
     */
    private String sanitizeQuery(String q) {
        return Arrays.stream(q.trim().split("\\s+"))
                .filter(StringUtils::hasText)
                .map(ClientUtils::escapeQueryChars)
                .collect(java.util.stream.Collectors.joining(" "));
    }

    private void addOrFilter(SolrQuery query, String field, List<String> values) {
        if (values == null || values.isEmpty()) return;
        List<String> cleaned = values.stream().filter(StringUtils::hasText).map(String::trim).toList();
        if (cleaned.isEmpty()) return;
        if (cleaned.size() == 1) {
            query.addFilterQuery(field + ":\"" + escape(cleaned.get(0)) + "\"");
        } else {
            String joined = String.join(" OR ", cleaned.stream().map(v -> field + ":\"" + escape(v) + "\"").toList());
            query.addFilterQuery("(" + joined + ")");
        }
    }

    private void applySort(SolrQuery query, String sort, boolean hasQuery) {
        String s = sort == null ? "" : sort.toLowerCase();
        switch (s) {
            case "title_asc" -> query.setSort("title_sort", SolrQuery.ORDER.asc);
            case "title_desc" -> query.setSort("title_sort", SolrQuery.ORDER.desc);
            case "date_desc" -> {
                query.setSort("publication_date", SolrQuery.ORDER.desc);
                query.addSort("title_sort", SolrQuery.ORDER.asc);
            }
            case "date_asc" -> {
                query.setSort("publication_date", SolrQuery.ORDER.asc);
                query.addSort("title_sort", SolrQuery.ORDER.asc);
            }
            default -> {
                if (hasQuery) {
                    query.setSort("score", SolrQuery.ORDER.desc);
                } else {
                    query.setSort("created_at", SolrQuery.ORDER.desc);
                }
            }
        }
    }

    private Map<String, List<SearchResponse.FacetValue>> extractFacets(QueryResponse response) {
        Map<String, List<SearchResponse.FacetValue>> out = new LinkedHashMap<>();
        if (response.getFacetFields() == null) return out;
        for (FacetField field : response.getFacetFields()) {
            List<SearchResponse.FacetValue> values = new ArrayList<>();
            if (field.getValues() != null) {
                for (FacetField.Count c : field.getValues()) {
                    if (c.getCount() > 0) values.add(new SearchResponse.FacetValue(c.getName(), c.getCount()));
                }
            }
            out.put(field.getName(), values);
        }
        return out;
    }

    private ResourceSummaryDto toSummary(SolrDocument doc) {
        Long id = Long.valueOf(String.valueOf(doc.getFieldValue("id")));
        LocalDate pubDate = null;
        Object year = doc.getFieldValue("publication_year");
        Object pdate = doc.getFieldValue("publication_date");
        if (pdate instanceof Date d) {
            pubDate = d.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        } else if (year != null) {
            try { pubDate = LocalDate.of(Integer.parseInt(String.valueOf(year)), 1, 1); } catch (Exception ignored) {}
        }
        return new ResourceSummaryDto(
                id,
                str(doc, "title"),
                str(doc, "description"),
                str(doc, "publisher"),
                pubDate,
                str(doc, "resource_type"),
                str(doc, "language"),
                str(doc, "isbn_or_identifier"),
                null,
                null,
                null,
                str(doc, "status"),
                strList(doc, "authors"),
                strList(doc, "subjects")
        );
    }

    private String str(SolrDocument doc, String field) {
        Object v = doc.getFieldValue(field);
        return v == null ? null : String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    private List<String> strList(SolrDocument doc, String field) {
        Object v = doc.getFieldValue(field);
        if (v == null) return List.of();
        if (v instanceof Collection<?> c) return c.stream().map(String::valueOf).toList();
        return List.of(String.valueOf(v));
    }

    private String escape(String v) {
        return v.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
