package edu.rpi.tw.twks.servlet.resource;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertTrue;

public final class AssertionsResourceTest extends AbstractResourceTest {
    @Test
    public void testGetAssertions() {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final String responseBody =
                target()
                        .path("/assertions")
                        .request(Lang.TRIG.getContentType().getContentType())
                        .get(String.class);
        final Model actual = ModelFactory.createDefaultModel();
        actual.read(new StringReader(responseBody), "", Lang.TRIG.getName());
        assertTrue(getTestData().specNanopublication.getAssertion().getModel().isIsomorphicWith(actual));
    }

    @Test
    public void testGetOntologyAssertions() {
        getTwks().putNanopublication(getTestData().ontologyNanopublication);
        final String responseBody =
                target()
                        .path("/assertions/ontology")
                        .queryParam("uri", getTestData().ontologyUri.toString())
                        .request(Lang.TRIG.getContentType().getContentType())
                        .get(String.class);
        final Model actual = ModelFactory.createDefaultModel();
        actual.read(new StringReader(responseBody), "", Lang.TRIG.getName());
        assertTrue(getTestData().ontologyNanopublication.getAssertion().getModel().isIsomorphicWith(actual));
    }
}
