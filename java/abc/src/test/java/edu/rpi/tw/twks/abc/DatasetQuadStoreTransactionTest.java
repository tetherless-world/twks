package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public final class DatasetQuadStoreTransactionTest {
    private final Uri testName = Uri.parse("http://example.com/model");
    private final Statement testStatement = ResourceFactory.createStatement(ResourceFactory.createResource("http://example.com/subject"), RDFS.comment, ResourceFactory.createPlainLiteral("comment"));
    private Dataset dataset;
    private Model testModel;

    private DatasetQuadStoreTransaction newTransaction(final ReadWrite readWrite) {
        final DatasetTransaction datasetTransaction = new DatasetTransaction(dataset, readWrite);
        return new DatasetQuadStoreTransaction(dataset, datasetTransaction);
    }

    @Before
    public void setUp() {
        dataset = DatasetFactory.create();
        testModel = ModelFactory.createDefaultModel();
        testModel.add(testStatement);
    }

    // Default Dataset doesn't support abort on transactions, only commit
//    @Test
//    public void testAbort() {
//        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
//            tx.addNamedGraph(testName, testModel);
//            tx.abort();
//        }
//        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
//            assertFalse(tx.containsNamedGraph(testName));
//        }
//    }

    @Test
    public void testAddNamedGraph() {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.addNamedGraph(testName, testModel);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertTrue(tx.containsNamedGraph(testName));
        }
    }

    @Test
    public void testClose() {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.commit();
        }
    }

    @Test
    public void testCommit() {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.addNamedGraph(testName, testModel);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertTrue(tx.containsNamedGraph(testName));
        }
    }

    @Test
    public void testContainsNamedGraph() {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertFalse(tx.containsNamedGraph(testName));
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.addNamedGraph(testName, testModel);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertTrue(tx.containsNamedGraph(testName));
        }
    }

    @Test
    public void testGetNamedGraph() throws Exception {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            try {
                tx.getNamedGraph(testName);
                fail();
            } catch (final NoSuchNamedGraphException e) {
            }
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.addNamedGraph(testName, testModel);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            final Model actual = tx.getNamedGraph(testName);
            assertTrue(actual.isIsomorphicWith(testModel));
        }
    }

    @Test
    public void testGetOrCreateNamedGraph() throws Exception {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.getOrCreateNamedGraph(testName).add(testModel);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            final Model actual = tx.getNamedGraph(testName);
            assertTrue(actual.isIsomorphicWith(testModel));
        }
    }

    @Test
    public void testRemoveAllGraphs() {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertFalse(tx.containsNamedGraph(testName));
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.addNamedGraph(testName, testModel);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertTrue(tx.containsNamedGraph(testName));
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.removeAllGraphs();
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertFalse(tx.containsNamedGraph(testName));
        }
    }

    @Test
    public void testRemoveNamedGraph() {
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertFalse(tx.containsNamedGraph(testName));
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.addNamedGraph(testName, testModel);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertTrue(tx.containsNamedGraph(testName));
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.WRITE)) {
            tx.removeNamedGraph(testName);
            tx.commit();
        }
        try (final DatasetQuadStoreTransaction tx = newTransaction(ReadWrite.READ)) {
            assertFalse(tx.containsNamedGraph(testName));
        }
    }
}
