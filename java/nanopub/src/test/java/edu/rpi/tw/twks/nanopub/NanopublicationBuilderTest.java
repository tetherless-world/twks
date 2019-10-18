package edu.rpi.tw.twks.nanopub;

import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;

public final class NanopublicationBuilderTest {
    private final Statement assertionStatement = ResourceFactory.createStatement(ResourceFactory.createResource("http://example.org/trastuzumab"), ResourceFactory.createProperty("is-indicated-for"), ResourceFactory.createResource("breast-cancer"));

    @Test
    public void testMinimal() {
        final NanopublicationBuilder builder = new NanopublicationBuilder();
        builder.getAssertionBuilder().getModel().add(assertionStatement);
        assertNotSame(builder.build(), null);
    }
}
