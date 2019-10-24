package edu.rpi.tw.twks.test;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import edu.rpi.tw.twks.api.BulkReadApi;
import edu.rpi.tw.twks.api.BulkWriteApi;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.QueryApi;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.MoreDatasetFactory;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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

    @BeforeClass
    public static void setUpClass() throws Exception {
        testData = new TestData();
    }

    protected final Path getTempDirPath() {
        return tempDirPath;
    }

    protected final SystemUnderTestT getSystemUnderTest() {
        return sut;
    }

    @Before
    public final void setUp() throws Exception {
        tempDirPath = Files.createTempDirectory(getClass().getSimpleName());
        sut = openSystemUnderTest();
    }

    @After
    public final void tearDown() throws Exception {
        sut.deleteNanopublication(testData.secondNanopublication.getUri());
        sut.deleteNanopublication(testData.specNanopublication.getUri());
        closeSystemUnderTest(sut);
        MoreFiles.deleteRecursively(tempDirPath, RecursiveDeleteOption.ALLOW_INSECURE);
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
    public void testDump() throws Exception {
        if (!(sut instanceof BulkWriteApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication);

        ((BulkWriteApi) sut).dump();

        final List<Path> filePaths = Files.walk(tempDirPath).collect(Collectors.toList());

        for (final Path filePath : filePaths) {
            if (Files.isRegularFile(filePath) && filePath.getFileName().endsWith(".trig")) {
                return;
            }
        }
        fail();
    }

    @Test
    public void testGetAssertions() {
        if (!(sut instanceof BulkReadApi)) {
            return;
        }

//        assertTrue(((BulkReadApi) sut).getAssertions().isEmpty());
        sut.putNanopublication(testData.specNanopublication);
        {
            final Model assertions = ((BulkReadApi) sut).getAssertions();
            if (!assertions.isIsomorphicWith(testData.specNanopublication.getAssertion().getModel())) {
//                assertions.write(System.out, Lang.TRIG.getName());
                fail();
            }
        }
        sut.putNanopublication(testData.secondNanopublication);
        assertFalse(((BulkReadApi) sut).getAssertions().isIsomorphicWith(testData.specNanopublication.getAssertion().getModel()));
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
        if (!(sut instanceof QueryApi)) {
            return;
        }

        sut.putNanopublication(testData.specNanopublication);
        sut.putNanopublication(testData.secondNanopublication);

        final Query query = QueryFactory.create("CONSTRUCT WHERE { ?S ?P ?O }");
        final Model sutAssertionsModel;
        try (final QueryExecution queryExecution = ((QueryApi) sut).queryAssertions(query)) {
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
        if (!(sut instanceof QueryApi)) {
            return;
        }

        // SPARQL 1.1 can't do CONSTRUCT WHERE { GRAPH ?G { ?S ?P ?O } }
        final Query query = QueryFactory.create("SELECT ?G ?S ?P ?O WHERE { GRAPH ?G { ?S ?P ?O } }");
        try (final QueryExecution queryExecution = ((QueryApi) sut).queryNanopublications(query)) {
            if (queryExecution.execSelect().hasNext()) {
                fail();
            }
        }

        sut.putNanopublication(testData.specNanopublication);


        final Dataset actualDataset;
        try (final QueryExecution queryExecution = ((QueryApi) sut).queryNanopublications(query)) {
            actualDataset = MoreDatasetFactory.createDatasetFromResultSet(queryExecution.execSelect());
        }
        final Nanopublication actual = NanopublicationFactory.getInstance().createNanopublicationFromDataset(actualDataset);

        assertTrue(actual.isIsomorphicWith(testData.specNanopublication));
    }

    @Test
    public void testPutNanopublication() {
        sut.putNanopublication(testData.specNanopublication);
    }

    protected abstract SystemUnderTestT openSystemUnderTest() throws Exception;
}
