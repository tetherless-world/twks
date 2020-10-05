package edu.rpi.tw.twks.text;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class FullTextSearchableDatasetFactoryTest {
    @Test
    public void testCreate() {
        final FullTextSearchConfiguration configuration = FullTextSearchConfiguration.builder().setEnable(true).build();
        final Dataset wrappedDataset = DatasetFactory.create();
        assertNotSame(null, FullTextSearchableDatasetFactory.getInstance().createFullTextSearchableDataset(configuration, wrappedDataset));
    }
}
