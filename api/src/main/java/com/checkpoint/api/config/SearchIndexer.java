package com.checkpoint.api.config;

import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManagerFactory;

/**
 * Builds the Hibernate Search / Lucene index on application startup.
 * Re-indexes all existing database data so that full-text search is available immediately.
 */
@Component
public class SearchIndexer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexer.class);

    private final EntityManagerFactory entityManagerFactory;

    public SearchIndexer(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting Hibernate Search mass indexing...");

        MassIndexer indexer = Search.mapping(entityManagerFactory)
                .scope(Object.class)
                .massIndexer()
                .threadsToLoadObjects(2);

        indexer.startAndWait();

        log.info("Hibernate Search mass indexing completed.");
    }
}
