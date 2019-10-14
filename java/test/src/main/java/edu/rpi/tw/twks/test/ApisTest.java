package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.SparqlQueryApi;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public abstract class ApisTest<SystemUnderTestT extends NanopublicationCrudApi> {
    private static TestData testData;
    private SystemUnderTestT sut;

    @BeforeClass
    public static void setUpClass() throws Exception {
        testData = new TestData();
    }

    @Before
    public void setUp() throws Exception {
        sut = openSystemUnderTest();
    }

    @After
    public void tearDown() throws Exception {
        sut.deleteNanopublication(testData.secondNanopublication.getUri());
        sut.deleteNanopublication(testData.specNanopublication.getUri());
        closeSystemUnderTest(sut);
    }

    protected abstract void closeSystemUnderTest(SystemUnderTestT sut);

    @Test
    public void testDeleteNanopublicationAbsent() {
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, sut.deleteNanopublication(testData.specNanopublication.getUri()));
    }

    @Test
    public void testDeleteNanopublicationPresent() {
        sut.putNanopublication(testData.specNanopublication);
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, sut.deleteNanopublication(testData.specNanopublication.getUri()));
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, sut.deleteNanopublication(testData.specNanopublication.getUri()));
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
    public void testQueryAssertions() {
        if (!(sut instanceof SparqlQueryApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication);
        sut.putNanopublication(testData.secondNanopublication);

        final Query query = QueryFactory.create("CONSTRUCT WHERE { ?S ?P ?O }");
        final Model sutAssertionsModel;
        try (final QueryExecution queryExecution = ((SparqlQueryApi) sut).queryAssertions(query)) {
            sutAssertionsModel = queryExecution.execConstruct();
        }

//            sutAssertionsModel.write(System.out, "TURTLE");
        assertEquals(2, sutAssertionsModel.listStatements().toList().size());
        final Statement statement1 = testData.specNanopublication.getAssertion().getModel().listStatements().toList().get(0);
        assertEquals(1, sutAssertionsModel.listStatements(statement1.getSubject(), statement1.getPredicate(), statement1.getObject()).toList().size());
        final Statement statement2 = testData.secondNanopublication.getAssertion().getModel().listStatements().toList().get(0);
        assertEquals(1, sutAssertionsModel.listStatements(statement2.getSubject(), statement2.getPredicate(), statement2.getObject()).toList().size());
    }

    @Test
    public void testQueryNanopublications() {
        if (!(sut instanceof SparqlQueryApi)) {
            return;
        }

        final Query query = QueryFactory.create("CONSTRUCT WHERE { ?S ?P ?O }");
        try (final QueryExecution queryExecution = ((SparqlQueryApi) sut).queryNanopublications(query)) {
            if (!queryExecution.execConstruct().isEmpty()) {
                fail();
            }
        }

        sut.putNanopublication(testData.specNanopublication);

        final Model sutUnionModel;
        try (final QueryExecution queryExecution = ((SparqlQueryApi) sut).queryNanopublications(query)) {
            sutUnionModel = queryExecution.execConstruct();
        }

        assertTrue(sutUnionModel.isIsomorphicWith(testData.specNanopublication.toDataset().getUnionModel()));
    }

    @Test
    public void testPutNanopublication() {
        sut.putNanopublication(testData.specNanopublication);
    }

    protected abstract SystemUnderTestT openSystemUnderTest() throws Exception;
}
