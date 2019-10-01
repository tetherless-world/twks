package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.MalformedNanopublicationException;
import edu.rpi.tw.twdb.api.Nanopublication;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public final class NanopublicationParserImplTest {
    private NanopublicationParserImpl sut;
    private TestData testData;

    @Before
    public void setUp() throws IOException {
        this.sut = new NanopublicationParserImpl(new NanopublicationFactoryImpl());
        this.testData = new TestData();
    }

    /**
     * Parse the specification's well-formed nanopublication.
     */
    @Test
    public void testParseNanopublicationFile() throws IOException, MalformedNanopublicationException {
        final Nanopublication nanopublication = sut.parse(testData.specNanopublicationFilePath);
        assertEquals("http://example.org/pub1", Uris.toString(nanopublication.getUri()));
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(3, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(2, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
    }

    @Test
    public void testParseAssertionFile() throws IOException, MalformedNanopublicationException {
        final Nanopublication nanopublication = sut.parse(testData.assertionOnlyFilePath);
        assertEquals(testData.assertionOnlyFilePath.toURI().toString(), Uris.toString(nanopublication.getUri()));
        assertEquals(1, nanopublication.getAssertion().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getProvenance().getModel().listStatements().toList().size());
        assertEquals(1, nanopublication.getPublicationInfo().getModel().listStatements().toList().size());
    }
}
