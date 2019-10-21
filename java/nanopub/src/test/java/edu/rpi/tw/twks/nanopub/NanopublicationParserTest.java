package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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
        final ImmutableList<Nanopublication> nanopublications = sut.parse(testData.specNanopublicationFilePath);
        assertEquals(1, nanopublications.size());
        final Nanopublication nanopublication = nanopublications.get(0);
        assertEquals("http://example.org/pub1", nanopublication.getUri().toString());
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
        NanopublicationFactory.getInstance().createNanopublicationsFromDataset(nanopublication.toDataset());
    }

    @Test
    public void testParseAssertionFile() throws IOException, MalformedNanopublicationException {
        final ImmutableList<Nanopublication> nanopublications = sut.parse(testData.assertionOnlyFilePath);
        assertEquals(1, nanopublications.size());
        final Nanopublication nanopublication = nanopublications.get(0);
//        assertEquals(testData.assertionOnlyFilePath.toURI().toString(), nanopublication.getUri().toString());
        assertTrue(nanopublication.getUri().toString().startsWith("urn:uuid:"));
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
        NanopublicationFactory.getInstance().createNanopublicationsFromDataset(nanopublication.toDataset()).get(0);
    }

    @Test
    public void testMultipleUniqueNanopublicationsFile() throws Exception {
        final ImmutableList<Nanopublication> nanopublications = sut.parse(testData.uniqueNanopublicationsFilePath);
        assertEquals(2, nanopublications.size());
        final Map<String, Nanopublication> nanopublicationsByUri = nanopublications.stream().collect(Collectors.toMap(nanopublication -> nanopublication.getUri().toString(), nanopublication -> nanopublication));
        assertNotSame(null, nanopublicationsByUri.get("http://example.org/pub1"));
        assertNotSame(null, nanopublicationsByUri.get("http://example.org/pub2"));
    }

    @Test
    public void testWhyisParseNanopublicationFile() throws IOException, MalformedNanopublicationException {
        final ImmutableList<Nanopublication> nanopublications = sut.setDialect(NanopublicationDialect.WHYIS).parse(testData.whyisNanopublicationFilePath);
        assertEquals(1, nanopublications.size());
        final Nanopublication nanopublication = nanopublications.get(0);
        assertEquals("http://localhost:5000/pub/0ac4b5ae-ad66-11e9-b097-3af9d3cf1ae5", nanopublication.getUri().toString());
        assertEquals(5, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(0, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(5, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
        // Test that we can decompose nanopublications we generate
        NanopublicationFactory.getInstance().createNanopublicationsFromDataset(nanopublication.toDataset(), NanopublicationDialect.WHYIS);
    }
}

