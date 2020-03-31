package edu.rpi.tw.twks.test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.api.*;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.MoreDatasetFactory;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
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

import static edu.rpi.tw.twks.test.ModelAssert.assertModelEquals;
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

    private void deleteTestDataNanopublications() {
        // Can't call deleteNanopublications() because the external API clients don't support it (intentionally).
        sut.deleteNanopublications(ImmutableList.of(
                testData.ontologyNanopublication.getUri(),
                testData.secondNanopublication.getUri(),
                testData.secondOntologyNanopublication.getUri(),
                testData.specNanopublication.getUri()
        ));
    }

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
        deleteTestDataNanopublications();
//        sut.deleteNanopublications();
    }

    @After
    public final void tearDown() throws Exception {
        deleteTestDataNanopublications();
//        sut.deleteNanopublications();
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
    public void testGetAssertionsAfterDelete() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        // No assertions
//        assertTrue(((BulkReadApi) sut).getAssertions().isEmpty());

        // Add first nanopublication
        sut.putNanopublication(testData.specNanopublication);

        // Assert first nanopublication's assertions only
        assertModelEquals(((GetAssertionsApi) sut).getAssertions(), testData.specNanopublication.getAssertion().getModel());

        // Add second nanopublication
        sut.putNanopublication(testData.secondNanopublication);

        {
            final Model expectedAssertions = ModelFactory.createDefaultModel();
            expectedAssertions.add(testData.specNanopublication.getAssertion().getModel());
            expectedAssertions.add(testData.secondNanopublication.getAssertion().getModel());
            assertModelEquals(((GetAssertionsApi) sut).getAssertions(), expectedAssertions);
        }

        // Remove first nanopublication
        sut.deleteNanopublication(testData.specNanopublication.getUri());

        // Assert second nanopublication's assertions only
        assertModelEquals(((GetAssertionsApi) sut).getAssertions(), testData.secondNanopublication.getAssertion().getModel());
    }

    @Test
    public void testGetAssertionsEmpty() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        assertTrue(((GetAssertionsApi) sut).getAssertions().isEmpty());
    }

    @Test
    public void testGetAssertionsOne() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        // No assertions
//        assertTrue(((BulkReadApi) sut).getAssertions().isEmpty());

        sut.putNanopublication(testData.specNanopublication);

        assertModelEquals(((GetAssertionsApi) sut).getAssertions(), testData.specNanopublication.getAssertion().getModel());
    }

    @Test
    public void testGetAssertionsTwo() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        // No assertions
//        assertTrue(((BulkReadApi) sut).getAssertions().isEmpty());

        // Add first nanopublication
        sut.putNanopublication(testData.specNanopublication);

        // Assert first nanopublication's assertions only
        assertModelEquals(((GetAssertionsApi) sut).getAssertions(), testData.specNanopublication.getAssertion().getModel());

        // Add second nanopublication
        sut.putNanopublication(testData.secondNanopublication);

        {
            final Model expectedAssertions = ModelFactory.createDefaultModel();
            expectedAssertions.add(testData.specNanopublication.getAssertion().getModel());
            expectedAssertions.add(testData.secondNanopublication.getAssertion().getModel());
            assertModelEquals(((GetAssertionsApi) sut).getAssertions(), expectedAssertions);
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
    public void testGetOntologyAssertionsOneNanopublicationOneOntology() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.ontologyNanopublication);
        assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri)), testData.ontologyNanopublication.getAssertion().getModel());
    }

    @Test
    public void testGetOntologyAssertionsThreeNanopublicationsOneOntology() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication); // No ontology
        sut.putNanopublication(testData.secondNanopublication); // No ontology
        sut.putNanopublication(testData.ontologyNanopublication);
        assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri)), testData.ontologyNanopublication.getAssertion().getModel());
    }

    @Test
    public void testGetOntologyAssertionsTwoNanopublicationsOneOntologyDelete() throws Exception {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.ontologyNanopublication);
        final Nanopublication secondOntologyNanopublication;
        {
            // Use the second nanopublication with the first ontology URI
            final Model ontologyNanopublicationAssertions = ModelFactory.createDefaultModel().add(testData.secondNanopublication.getAssertion().getModel());
            ontologyNanopublicationAssertions.add(ResourceFactory.createResource(testData.ontologyUri.toString()), RDF.type, OWL.Ontology);
            secondOntologyNanopublication = Nanopublication.builder().getAssertionBuilder().setModel(ontologyNanopublicationAssertions).getNanopublicationBuilder().build();
//            ontologyNanopublicationAssertions.listStatements().forEachRemaining(statement -> System.out.println(statement));
        }

        sut.putNanopublication(secondOntologyNanopublication);

        try {
            {
                final Model expectedAssertions = ModelFactory.createDefaultModel();
                expectedAssertions.add(testData.ontologyNanopublication.getAssertion().getModel());
                expectedAssertions.add(testData.secondNanopublication.getAssertion().getModel());
                assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri)), expectedAssertions);
            }

            sut.deleteNanopublication(testData.ontologyNanopublication.getUri());

            assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri)), secondOntologyNanopublication.getAssertion().getModel());

            sut.deleteNanopublication(secondOntologyNanopublication.getUri());

            {
                final Model actualAssertions = ((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri));
                assertTrue(actualAssertions.isEmpty());
            }
        } finally {
            sut.deleteNanopublication(secondOntologyNanopublication.getUri());
        }
    }

    @Test
    public void testGetOntologyAssertionsTwoNanopublicationsTwoOntologies() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.ontologyNanopublication);
        sut.putNanopublication(testData.secondOntologyNanopublication);

        assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri)), testData.ontologyNanopublication.getAssertion().getModel());

        {
            final Model expectedAssertions = ModelFactory.createDefaultModel();
            expectedAssertions.add(testData.ontologyNanopublication.getAssertion().getModel());
            expectedAssertions.add(testData.secondOntologyNanopublication.getAssertion().getModel());
            assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri, testData.secondOntologyUri)), expectedAssertions);
        }
    }

    @Test
    public void testGetOntologyAssertionsTwoNanopublicationsTwoOntologiesDelete() {
        if (!(sut instanceof GetAssertionsApi)) {
            return;
        }

        sut.putNanopublication(testData.ontologyNanopublication);
        sut.putNanopublication(testData.secondOntologyNanopublication);

        assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri)), testData.ontologyNanopublication.getAssertion().getModel());

        {
            final Model expectedAssertions = ModelFactory.createDefaultModel();
            expectedAssertions.add(testData.ontologyNanopublication.getAssertion().getModel());
            expectedAssertions.add(testData.secondOntologyNanopublication.getAssertion().getModel());
            assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.ontologyUri, testData.secondOntologyUri)), expectedAssertions);
        }

        sut.deleteNanopublication(testData.ontologyNanopublication.getUri());

        assertModelEquals(((GetAssertionsApi) sut).getOntologyAssertions(ImmutableSet.of(testData.secondOntologyUri)), testData.secondOntologyNanopublication.getAssertion().getModel());
    }

    @Test
    public void testIsEmpty() {
        if (!(sut instanceof IsEmptyApi)) {
            return;
        }

        assertTrue(((IsEmptyApi) sut).isEmpty());
        sut.putNanopublication(testData.ontologyNanopublication);
        assertFalse(((IsEmptyApi) sut).isEmpty());
        sut.deleteNanopublications();
        assertTrue(((IsEmptyApi) sut).isEmpty());
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

        final Model expectedAssertions = ModelFactory.createDefaultModel();
        expectedAssertions.add(testData.specNanopublication.getAssertion().getModel());
        expectedAssertions.add(testData.secondNanopublication.getAssertion().getModel());

        final Query query = QueryFactory.create("CONSTRUCT WHERE { ?S ?P ?O }");
        final Model actualAssertions;
        try (final QueryExecution queryExecution = ((AssertionQueryApi) sut).queryAssertions(query)) {
            actualAssertions = queryExecution.execConstruct();
        }

        assertModelEquals(actualAssertions, expectedAssertions);
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
        final Nanopublication actual = NanopublicationParser.DEFAULT.parseDataset(actualDataset).get(0);

        assertTrue(actual.isIsomorphicWith(testData.specNanopublication));
    }
}
