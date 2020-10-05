package edu.rpi.tw.twks.text;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

public final class FullTextSearchableDatasetFactory {
    private final static FullTextSearchableDatasetFactory instance = new FullTextSearchableDatasetFactory();

    public final static FullTextSearchableDatasetFactory getInstance() {
        return instance;
    }

    public final Dataset createFullTextSearchableDataset(final FullTextSearchConfiguration configuration, final Dataset wrapDataset) {
        final Directory directory;
        if (configuration.getLuceneDirectoryPath().isPresent()) {
            throw new UnsupportedOperationException();
        } else {
            directory = new RAMDirectory();
        }

        final EntityDefinition entityDefinition = new EntityDefinition("uri", "text");
        entityDefinition.setGraphField("graph");
        entityDefinition.setPrimaryPredicate(RDFS.label.asNode());
        // Add additional predicates for the "text" field
//        entityDefinition.set("text", DCTerms.title.asNode());
//        entityDefinition.set("text", DC_11.title.asNode());

        return org.apache.jena.query.text.TextDatasetFactory.createLucene(wrapDataset, directory, new TextIndexConfig(entityDefinition));
    }
}
