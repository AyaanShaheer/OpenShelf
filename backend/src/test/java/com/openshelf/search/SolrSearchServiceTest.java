package com.openshelf.search;

import com.openshelf.common.exception.ServiceUnavailableException;
import com.openshelf.search.dto.SearchRequest;
import com.openshelf.search.service.SolrSearchService;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolrSearchServiceTest {

    @Mock private SolrClient solrClient;

    @Test
    void escapesUserQueryAndUsesDocValuesTitleSort() throws Exception {
        SolrDocumentList docs = new SolrDocumentList();
        docs.setNumFound(0);
        NamedList<Object> response = new NamedList<>();
        response.add("response", docs);
        when(solrClient.query(any(SolrParams.class))).thenReturn(new QueryResponse(response, solrClient));

        SolrSearchService service = new SolrSearchService(solrClient);
        service.search(new SearchRequest("open (scholarship)", List.of("BOOK"), null, null,
                null, null, "title_asc", 0, 10));

        ArgumentCaptor<SolrParams> captor = ArgumentCaptor.forClass(SolrParams.class);
        verify(solrClient).query(captor.capture());
        SolrQuery query = (SolrQuery) captor.getValue();

        assertThat(query.getQuery()).isEqualTo("open \\(scholarship\\)");
        assertThat(query.getSortField()).isEqualTo("title_sort asc");
        assertThat(query.getFilterQueries()).anyMatch(f -> f.startsWith("status:ACTIVE"))
                .anyMatch(f -> f.contains("resource_type:\"BOOK\""));
    }

    @Test
    void mapsSolrFailureToServiceUnavailable() throws Exception {
        when(solrClient.query(any(SolrParams.class)))
                .thenThrow(new SolrServerException("Connection refused"));

        SolrSearchService service = new SolrSearchService(solrClient);

        assertThatThrownBy(() -> service.search(new SearchRequest(null, null, null, null,
                null, null, null, 0, 10)))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("temporarily unavailable");
    }
}
