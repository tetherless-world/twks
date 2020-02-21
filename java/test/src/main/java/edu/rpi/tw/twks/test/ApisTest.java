package edu.rpi.tw.twks.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.api.*;
import edu.rpi.tw.twks.nanopub.DatasetNanopublications;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.MoreDatasetFactory;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public abstract class ApisTest<SystemUnderTestT extends NanopublicationCrudApi> {
    private static TestData testData;
    private SystemUnderTestT sut;
    private Path tempDirPath;

    public static void checkDump(final Path dumpDirectoryPath) throws IOException {
        final List<Path> filePaths = Files.walk(dumpDirectoryPath).collect(Collectors.toList());

        for (final Path filePath : filePaths) {
            if (!Files.isRegularFile(filePath)) {
                continue;
            }
            final String fileName = filePath.getFileName().toString();
            if (fileName.endsWith(".trig")) {
                return;
            }
        }
        fail();
    }

    protected final static TestData getTestData() {
        return testData;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        testData = new TestData();
    }

    protected abstract void closeSystemUnderTest(SystemUnderTestT sut);

    protected final SystemUnderTestT getSystemUnderTest() {
        return sut;
    }

    protected final Path getTempDirPath() {
        return tempDirPath;
    }

    protected abstract SystemUnderTestT openSystemUnderTest() throws Exception;

    @Before
    public final void setUp() throws Exception {
        tempDirPath = Files.createTempDirectory(getClass().getSimpleName());
        sut = openSystemUnderTest();
    }

    @After
    public final void tearDown() throws Exception {
        sut.deleteNanopublications(ImmutableList.of(
                testData.ontologyNanopublication.getUri(),
                testData.secondNanopublication.getUri(),
                testData.secondOntologyNanopublication.getUri(),
                testData.specNanopublication.getUri()
        ));
        closeSystemUnderTest(sut);
        MoreFiles.deleteRecursively(tempDirPath, RecursiveDeleteOption.ALLOW_INSECURE);
    }

    @Test
    public void testDeleteAllNanopublications() {
        sut.putNanopublication(getTestData().specNanopublication);
        assertTrue(sut.getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
        sut.putNanopublication(getTestData().secondNanopublication);
        assertTrue(sut.getNanopublication(getTestData().secondNanopublication.getUri()).isPresent());
        try {
            sut.deleteNanopublications();
        } catch (final UnsupportedOperationException e) {
            return;
        }
        assertFalse(sut.getNanopublication(getTestData().specNanopublication.getUri()).isPresent());
        assertFalse(sut.getNanopublication(getTestData().secondNanopublication.getUri()).isPresent());
    }

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
    public void testDeleteNanopublicationsAbsent() throws Exception {
        final ImmutableList<NanopublicationCrudApi.DeleteNanopublicationResult> results = sut.deleteNanopublications(ImmutableList.of(testData.specNanopublication.getUri(), testData.secondNanopublication.getUri()));
        assertEquals(2, results.size());
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, results.get(0));
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, results.get(1));
    }

    @Test
    public void testDeleteNanopublicationsMixed() {
        sut.putNanopublication(getTestData().specNanopublication);
        final ImmutableList<NanopublicationCrudApi.DeleteNanopublicationResult> results = sut.deleteNanopublications(ImmutableList.of(testData.specNanopublication.getUri(), testData.secondNanopublication.getUri()));
        assertEquals(2, results.size());
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, results.get(0));
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.NOT_FOUND, results.get(1));
    }

    @Test
    public void testDeleteNanopublicationsPresent() throws Exception {
        sut.putNanopublication(getTestData().specNanopublication);
        sut.putNanopublication(getTestData().secondNanopublication);
        final ImmutableList<NanopublicationCrudApi.DeleteNanopublicationResult> results = sut.deleteNanopublications(ImmutableList.of(testData.specNanopublication.getUri(), testData.secondNanopublication.getUri()));
        assertEquals(2, results.size());
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, results.get(0));
        assertEquals(NanopublicationCrudApi.DeleteNanopublicationResult.DELETED, results.get(1));
    }

    @Test
    public void testDump() throws Exception {
        if (!(sut instanceof AdministrationApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication);

        ((AdministrationApi) sut).dump();

        checkDump(tempDirPath);
    }

    @Test
    public void testGetAssertions() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        // No assertions
//        assertTrue(((BulkReadApi) sut).getAssertions().isEmpty());

        // Add first nanopublication
        sut.putNanopublication(testData.specNanopublication);

        // Assert first nanopublication's assertions only
        {
            final Model actualAssertions = ((GetAssertionsApi) sut).getAssertions();
            if (!actualAssertions.isIsomorphicWith(testData.specNanopublication.getAssertion().getModel())) {
//                assertions.write(System.out, Lang.TRIG.getName());
                fail();
            }
        }

        // Add second nanopublication
        sut.putNanopublication(testData.secondNanopublication);

        {
            final Model actualAssertions = ((GetAssertionsApi) sut).getAssertions();
            final Model expectedAssertions = ModelFactory.createDefaultModel();
            expectedAssertions.add(testData.specNanopublication.getAssertion().getModel());
            expectedAssertions.add(testData.secondNanopublication.getAssertion().getModel());
            assertTrue(expectedAssertions.isIsomorphicWith(actualAssertions));
        }

        // Remove first nanopublication
        sut.deleteNanopublication(testData.specNanopublication.getUri());

        // Assert second nanopublication's assertions only
        {
            final Model actualAssertions = ((GetAssertionsApi) sut).getAssertions();
            assertTrue(actualAssertions.isIsomorphicWith(testData.secondNanopublication.getAssertion().getModel()));
        }
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
    public void testGetOntologyAssertionsEmpty() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication);
        final Model assertions = ((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(Uri.parse("http://example.com/nonextant")));
        assertTrue(assertions.listStatements().toList().isEmpty());
    }

    @Test
    public void testGetOntologyAssertionsMixed() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication); // No ontology
        sut.putNanopublication(testData.secondNanopublication); // No ontology
        sut.putNanopublication(testData.ontologyNanopublication);
        final Model assertions = ((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri));
        assertTrue(assertions.isIsomorphicWith(testData.ontologyNanopublication.getAssertion().getModel()));
    }

    @Test
    public void testGetOntologyAssertionsOne() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.ontologyNanopublication);
        final Model assertions = ((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri));
        assertTrue(assertions.isIsomorphicWith(testData.ontologyNanopublication.getAssertion().getModel()));
        assertEquals(2, assertions.listStatements().toList().size());
    }

    @Test
    public void testGetOntologyAssertionsTwo() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.ontologyNanopublication);
        sut.putNanopublication(testData.secondOntologyNanopublication);

        {
            final Model assertions = ((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri));
            assertTrue(assertions.isIsomorphicWith(testData.ontologyNanopublication.getAssertion().getModel()));
            assertEquals(2, assertions.listStatements().toList().size());
        }
        {
            final Model assertions = ((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri, testData.secondOntologyUri));
            assertEquals(4, assertions.listStatements().toList().size());
        }
    }

    @Test
    public void testPostNanopublicationsAbsent() {
        final ImmutableList<NanopublicationCrudApi.PutNanopublicationResult> results = sut.postNanopublications(ImmutableList.of(testData.specNanopublication, testData.secondNanopublication));
        assertEquals(ImmutableList.of(NanopublicationCrudApi.PutNanopublicationResult.CREATED, NanopublicationCrudApi.PutNanopublicationResult.CREATED), results);
    }

    @Test
    public void testPostNanopublicationsMixed() {
        sut.putNanopublication(testData.specNanopublication);
        final ImmutableList<NanopublicationCrudApi.PutNanopublicationResult> results = sut.postNanopublications(ImmutableList.of(testData.specNanopublication, testData.secondNanopublication));
        assertEquals(ImmutableList.of(NanopublicationCrudApi.PutNanopublicationResult.OVERWROTE, NanopublicationCrudApi.PutNanopublicationResult.CREATED), results);
    }

    @Test
    public void testPostNanopublicationsPresent() {
        sut.postNanopublications(ImmutableList.of(testData.specNanopublication, testData.secondNanopublication));
        final ImmutableList<NanopublicationCrudApi.PutNanopublicationResult> results = sut.postNanopublications(ImmutableList.of(testData.specNanopublication, testData.secondNanopublication));
        assertEquals(ImmutableList.of(NanopublicationCrudApi.PutNanopublicationResult.OVERWROTE, NanopublicationCrudApi.PutNanopublicationResult.OVERWROTE), results);
    }

    @Test
    public void testPutNanopublicationAbsent() {
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.CREATED, sut.putNanopublication(testData.specNanopublication));
    }

    @Test
    public void testPutNanopublicationPresent() {
        sut.putNanopublication(testData.specNanopublication);
        assertEquals(NanopublicationCrudApi.PutNanopublicationResult.OVERWROTE, sut.putNanopublication(testData.specNanopublication));
    }

    @Test
    public void testQueryAssertions() {
        if (!(sut instanceof AssertionQueryApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication);
        sut.putNanopublication(testData.secondNanopublication);

        final Query query = QueryFactory.create("CONSTRUCT WHERE { ?S ?P ?O }");
        final Model sutAssertionsModel;
        try (final QueryExecution queryExecution = ((AssertionQueryApi) sut).queryAssertions(query)) {
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
    public void testQueryNanopublications() throws Exception {
        if (!(sut instanceof NanopublicationQueryApi)) {
            return;
        }

        // SPARQL 1.1 can't do CONSTRUCT WHERE { GRAPH ?G { ?S ?P ?O } }
        final Query query = QueryFactory.create("SELECT ?G ?S ?P ?O WHERE { GRAPH ?G { ?S ?P ?O } }");
        try (final QueryExecution queryExecution = ((NanopublicationQueryApi) sut).queryNanopublications(query)) {
            if (queryExecution.execSelect().hasNext()) {
                fail();
            }
        }

        sut.putNanopublication(testData.specNanopublication);


        final Dataset actualDataset;
        try (final QueryExecution queryExecution = ((NanopublicationQueryApi) sut).queryNanopublications(query)) {
            actualDataset = MoreDatasetFactory.createDatasetFromResultSet(queryExecution.execSelect());
        }
        final Nanopublication actual = DatasetNanopublications.copyOne(actualDataset);

        assertTrue(actual.isIsomorphicWith(testData.specNanopublication));
    }
}
