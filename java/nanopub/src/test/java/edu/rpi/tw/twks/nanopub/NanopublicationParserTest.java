package edu.rpi.tw.twks.nanopub;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class NanopublicationParserTest {
    private NanopublicationParser sut;
    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.sut = new NanopublicationParser();
        this.testData = new TestData();
    }

    @Test
    public void testSpecParseNanopublicationFile() throws IOException, MalformedNanopublicationException {
        final Nanopublication nanopublication = sut.parseOne(testData.specNanopublicationFilePath);
        assertEquals("http://example.org/pub1", nanopublication.getUri().toString());
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
        NanopublicationFactory.getInstance().createNanopublicationsFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testParseAssertionFile() throws IOException, MalformedNanopublicationException {
        final Nanopublication nanopublication = sut.parseOne(testData.assertionOnlyFilePath);
//        assertEquals(testData.assertionOnlyFilePath.toURI().toString(), nanopublication.getUri().toString());
        assertTrue(nanopublication.getUri().toString().startsWith("urn:uuid:"));
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
        NanopublicationFactory.getInstance().createNanopublicationFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testWhyisParseNanopublicationFile() throws IOException, MalformedNanopublicationException {
        final Nanopublication nanopublication = sut.setDialect(NanopublicationDialect.WHYIS).parseOne(testData.whyisNanopublicationFilePath);
        assertEquals("http://localhost:5000/pub/0ac4b5ae-ad66-11e9-b097-3af9d3cf1ae5", nanopublication.getUri().toString());
        assertEquals(5, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(0, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(5, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
        NanopublicationFactory.getInstance().createNanopublicationsFromDataset(nanopublication.toDataset(), NanopublicationDialect.WHYIS);
    }
}

