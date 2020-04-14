package edu.rpi.tw.twks.nanopub;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotNotFoundException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public final class NanopublicationParserTest {
    private NanopublicationParser sut;
    private TestData testData;

    private static Nanopublication parseOne(final Dataset dataset) {
        final ImmutableList<Nanopublication> nanopublications = NanopublicationParser.SPECIFICATION.parseDataset(dataset);
        assertEquals(1, nanopublications.size());
        return nanopublications.get(0);
    }

    @Before
    public void setUp() throws Exception {
        this.testData = new TestData();
    }

    @Test
    public void testBlankGraphName() {
        final String trig = "_:Na44d261588ba44bf88578b9f549b5e29 {\n" +
                "    <http://example.com/subject> <http://example.com/predicate> <http://example.com/object> .\n" +
                "}\n";
        try {
            NanopublicationParser.builder().setLang(Lang.TRIG).build().parseString(trig);
            fail();
        } catch (final MalformedNanopublicationRuntimeException e) {
        }
    }

    @Test
    public void testBrokenRdf() {
        try {
            NanopublicationParser.SPECIFICATION.parseString("broken RDF");
            fail();
        } catch (final MalformedNanopublicationRuntimeException e) {
        }
    }

    @Test
    public void testCreateNanopublicationFromDataset() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = parseOne(testData.specNanopublicationDataset);
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
    }

    @Test
    public void testCreateNanopublicationsFromDataset() throws MalformedNanopublicationException, IOException {
        final Dataset dataset = DatasetFactory.create();
        NanopublicationParser.SPECIFICATION.parseFile(testData.assertionOnlyFilePath).get(0).toDataset(dataset);
        parseOne(testData.specNanopublicationDataset).toDataset(dataset);
        assertEquals(8, ImmutableList.copyOf(dataset.listNames()).size());
        final ImmutableList<Nanopublication> nanopublications = NanopublicationParser.SPECIFICATION.parseDataset(dataset);
        assertEquals(2, nanopublications.size());
    }

    @Test
    public void testDuplicateNanopublications() {
        try {
            NanopublicationParser.SPECIFICATION.parseDataset(testData.duplicateNanopublicationsDataset);
            fail();
        } catch (final MalformedNanopublicationRuntimeException e) {
        }
    }

    @Test
    public void testIgnoreMalformedNanopublications() {
        try {
            NanopublicationParser.SPECIFICATION.parseFile(testData.mixFormedNanonpublicationFilePath);
            fail();
        } catch (final MalformedNanopublicationRuntimeException e) {
        }

        final List<Nanopublication> nanopublications = new ArrayList<>();
        NanopublicationParser.SPECIFICATION.parseFile(testData.mixFormedNanonpublicationFilePath, new NanopublicationConsumer() {
            @Override
            public void accept(final Nanopublication nanopublication) {
                nanopublications.add(nanopublication);
            }

            @Override
            public void onMalformedNanopublicationException(final MalformedNanopublicationException exception) {
            }
        });
        assertEquals(1, nanopublications.size());
        final Nanopublication nanopublication = nanopublications.get(0);
        assertEquals(Uri.parse("http://example.org/pub1"), nanopublication.getUri());
    }

    @Test
    public void testMissingFile() {
        try {
            NanopublicationParser.SPECIFICATION.parseFile(Paths.get("nonextantfile"));
            fail();
        } catch (final RiotNotFoundException e) {
        }
    }

    @Test
    public void testMultipleUniqueNanopublications() {
        final ImmutableList<Nanopublication> nanopublications = NanopublicationParser.SPECIFICATION.parseDataset(testData.uniqueNanopublicationsDataset);
        assertEquals(2, nanopublications.size());
        final Map<String, Nanopublication> nanopublicationsByUri = nanopublications.stream().collect(Collectors.toMap(nanopublication -> nanopublication.getUri().toString(), nanopublication -> nanopublication));
        assertNotSame(null, nanopublicationsByUri.get("http://example.org/pub1"));
        assertNotSame(null, nanopublicationsByUri.get("http://example.org/pub2"));
    }

    @Test
    public void testOverlappingNanopublications() {
        try {
            NanopublicationParser.SPECIFICATION.parseDataset(testData.overlappingNanopublicationsDataset);
            fail();
        } catch (final MalformedNanopublicationRuntimeException e) {
        }
    }

    @Test
    public void testParseAssertionFile() {
        final Nanopublication nanopublication = NanopublicationParser.SPECIFICATION.parseFile(testData.assertionOnlyFilePath).get(0);
//        assertEquals(testData.assertionOnlyFilePath.toURI().toString(), nanopublication.getUri().toString());
        assertTrue(nanopublication.getUri().toString().startsWith("urn:uuid:"));
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
//        NanopublicationFactory.DEFAULT.createNanopublicationFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testSpecParseNanopublicationFile() {
        final Nanopublication nanopublication = NanopublicationParser.SPECIFICATION.parseFile(testData.specNanopublicationFilePath).get(0);
        assertEquals("http://example.org/pub1", nanopublication.getUri().toString());
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
//        NanopublicationFactory.DEFAULT.createNanopublicationsFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testWhyisParseNanopublicationFile() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = NanopublicationParser.builder().setDialect(NanopublicationDialect.WHYIS).setLang(Lang.TRIG).build().parseFile(testData.whyisNanopublicationFilePath).get(0);
        // 20191120 Parser no longer preserves part names for non-specification dialects.
//        assertEquals("http://localhost:5000/pub/0ac4b5ae-ad66-11e9-b097-3af9d3cf1ae5", nanopublication.getUri().toString());
        assertEquals(5, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(5, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
//        new NanopublicationFactory(NanopublicationDialect.WHYIS).createNanopublicationsFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testLang() throws IOException {
        assertTrue(testData.specNanopublicationFilePath.toString().endsWith(".trig"));
        assertEquals(NanopublicationDialect.SPECIFICATION.getDefaultLang(), Lang.TRIG);
        assertEquals(NanopublicationDialect.WHYIS.getDefaultLang(), Lang.NQUADS);

        {
            // Lang will be inferred from the file path rather than using the dialect's default language
            NanopublicationParser.SPECIFICATION.parseFile(testData.specNanopublicationFilePath);
            NanopublicationParser.builder().setDialect(NanopublicationDialect.WHYIS).build().parseFile(testData.specNanopublicationFilePath);
        }

        final String specNanopublicationString = IOUtils.toString(testData.specNanopublicationFilePath.toUri(), Charsets.UTF_8);

        {
            // Lang will not be inferred from strings, will use the dialect's default language.
            NanopublicationParser.SPECIFICATION.parseString(specNanopublicationString); // Succeed because the dialect defaults to trig
            try {
                NanopublicationParser.builder().setDialect(NanopublicationDialect.WHYIS).build().parseString(specNanopublicationString);
                fail(); // Fail because the dialect defaults to nquads
            } catch (final Exception e) {
            }
        }

        {
            // Set the lang explicitly to override the dialect's default lang
            NanopublicationParser.builder().setDialect(NanopublicationDialect.WHYIS).setLang(Lang.TRIG).build().parseString(specNanopublicationString);
        }
    }
}

