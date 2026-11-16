package com.openshelf.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpJdkSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfig {

    @Bean
    public SolrClient solrClient(@Value("${app.solr.base-url}") String baseUrl,
                                 @Value("${app.solr.collection}") String collection) {
        String url = baseUrl.endsWith("/") ? baseUrl + collection : baseUrl + "/" + collection;
        // Use the JDK HTTP client to avoid a Jetty version clash between SolrJ 9.x and Spring Boot.
        return new HttpJdkSolrClient.Builder(url).build();
    }
}
