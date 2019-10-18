package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

import static org.junit.Assert.*;

public final class NanopublicationBuilderTest {
    private final Statement assertionStatement = ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/trastuzumab"), ResourceFactory.createProperty("is-indicated-for"), ResourceFactory.createResource("breast-cancer"));

    @Test
    public void testMinimal() {
        final NanopublicationBuilder builder = new NanopublicationBuilder();
        builder.getAssertionBuilder().getModel().add(assertionStatement);
        assertNotSame(builder.build(), null);
    }

    @Test
    public void testSetNanopublicationUri() {
        final Uri uri = Uri.parse("http://example.com/pub1");
        final NanopublicationBuilder builder = new NanopublicationBuilder().setUri(uri);
        builder.getAssertionBuilder().getModel().add(assertionStatement);
        final Nanopublication nanopublication = builder.build();
        assertEquals(uri, nanopublication.getUri());
        assertTrue(nanopublication.getAssertion().getName().toString().startsWith("urn:uuid:"));
        assertTrue(nanopublication.getHead().getName().toString().startsWith("urn:uuid:"));
        assertTrue(nanopublication.getProvenance().getName().toString().startsWith("urn:uuid:"));
        assertTrue(nanopublication.getPublicationInfo().getName().toString().startsWith("urn:uuid:"));
    }

    @Test
    public void testSetNanopublicationPartUri() {
        final Uri uri = Uri.parse("http://example.com/pub1#assertion");
        final NanopublicationBuilder builder = new NanopublicationBuilder();
        builder.getAssertionBuilder().setName(uri);
        builder.getAssertionBuilder().getModel().add(assertionStatement);
        final Nanopublication nanopublication = builder.build();
        assertTrue(nanopublication.getUri().toString().startsWith("urn:uuid:"));
        assertEquals(uri, nanopublication.getAssertion().getName());
    }
}
