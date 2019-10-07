package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;

public abstract class TwdbTest {
    private Twdb sut;
    private TestData testData;

    @Before
    public final void setUp() throws IOException, MalformedNanopublicationException {
        this.sut = newTdb();
        this.testData = new TestData();
    }

    @Test
    public void testDeleteNanopublicationAbsent() {
        assertFalse(sut.deleteNanopublication(testData.specNanopublication.getUri()));
    }

    @Test
    public void testDeleteNanopublicationPresent() {
        sut.putNanopublication(testData.specNanopublication);
        assertTrue(sut.deleteNanopublication(testData.specNanopublication.getUri()));
        assertFalse(sut.deleteNanopublication(testData.specNanopublication.getUri()));
    }

    @Test
    public void testGetNanopublicationAbsent() throws MalformedNanopublicationException {
        final Optional<Nanopublication> actual = sut.getNanopublication(testData.specNanopublication.getUri());
        assertFalse(actual.isPresent());
    }

    @Test
    public void testGetNanopublicationPresent() throws MalformedNanopublicationException {
        final Nanopublication expected = testData.specNanopublication;
        sut.putNanopublication(expected);
        final Nanopublication actual = sut.getNanopublication(expected.getUri()).get();
        assertNotSame(expected, actual);
//        RDFDataMgr.write(System.out, actual.toDataset(), Lang.TRIG);
        assertTrue(actual.isIsomorphicWith(expected));
    }

    @Test
    public void testPutNanopublication() {
        sut.putNanopublication(testData.specNanopublication);
    }

    @Test
    public void testQueryAssertions() {
        try (final TwdbTransaction transaction = sut.beginTransaction(ReadWrite.WRITE)) {
            sut.putNanopublication(testData.specNanopublication, transaction);
            sut.putNanopublication(testData.secondNanopublication, transaction);

            final Query query = QueryFactory.create("CONSTRUCT WHERE { ?S ?P ?O }");
            final Model sutAssertionsModel;
            try (final QueryExecution queryExecution = sut.queryAssertions(query, transaction)) {
                sutAssertionsModel = queryExecution.execConstruct();
            }

//            sutAssertionsModel.write(System.out, "TURTLE");
            assertEquals(2, sutAssertionsModel.listStatements().toList().size());
            final Statement statement1 = testData.specNanopublication.getAssertion().getModel().listStatements().toList().get(0);
            assertEquals(1, sutAssertionsModel.listStatements(statement1.getSubject(), statement1.getPredicate(), statement1.getObject()).toList().size());
            final Statement statement2 = testData.secondNanopublication.getAssertion().getModel().listStatements().toList().get(0);
            assertEquals(1, sutAssertionsModel.listStatements(statement2.getSubject(), statement2.getPredicate(), statement2.getObject()).toList().size());

            transaction.commit();
        }
    }

    @Test
    public void testQueryNanopublications() {
        final Query query = QueryFactory.create("CONSTRUCT WHERE { ?S ?P ?O }");
        try (final TwdbTransaction transaction = sut.beginTransaction(ReadWrite.WRITE)) {
            try (final QueryExecution queryExecution = sut.queryNanopublications(query, transaction)) {
                if (!queryExecution.execConstruct().isEmpty()) {
                    fail();
                }
            }

            sut.putNanopublication(testData.specNanopublication, transaction);

            final Model sutUnionModel;
            try (final QueryExecution queryExecution = sut.queryNanopublications(query, transaction)) {
                sutUnionModel = queryExecution.execConstruct();
            }

            assertTrue(sutUnionModel.isIsomorphicWith(testData.specNanopublication.toDataset().getUnionModel()));

            transaction.commit();
        }
    }

    protected abstract Twdb newTdb();
}
