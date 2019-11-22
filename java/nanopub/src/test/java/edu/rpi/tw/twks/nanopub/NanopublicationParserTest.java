package edu.rpi.tw.twks.nanopub;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class NanopublicationParserTest {
    private NanopublicationParser sut;
    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.testData = new TestData();
    }

    @Test
    public void testParseAssertionFile() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = NanopublicationParser.builder().setSource(testData.assertionOnlyFilePath).build().parseOne();
//        assertEquals(testData.assertionOnlyFilePath.toURI().toString(), nanopublication.getUri().toString());
        assertTrue(nanopublication.getUri().toString().startsWith("urn:uuid:"));
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
//        NanopublicationFactory.DEFAULT.createNanopublicationFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testSpecParseNanopublicationFile() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = NanopublicationParser.builder().setSource(testData.specNanopublicationFilePath).build().parseOne();
        assertEquals("http://example.org/pub1", nanopublication.getUri().toString());
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
//        NanopublicationFactory.DEFAULT.createNanopublicationsFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testWhyisParseNanopublicationFile() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = NanopublicationParser.builder().setDialect(NanopublicationDialect.WHYIS).setSource(testData.whyisNanopublicationFilePath).build().parseOne();
        // 20191120 Parser no longer preserves part names for non-specification dialects.
//        assertEquals("http://localhost:5000/pub/0ac4b5ae-ad66-11e9-b097-3af9d3cf1ae5", nanopublication.getUri().toString());
        assertEquals(5, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(5, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
//        new NanopublicationFactory(NanopublicationDialect.WHYIS).createNanopublicationsFromDataset(nanopublication.toDataset());
    }
}

