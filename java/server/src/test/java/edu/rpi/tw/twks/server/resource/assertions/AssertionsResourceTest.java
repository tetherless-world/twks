package edu.rpi.tw.twks.server.resource.assertions;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AbstractResourceTest;
import edu.rpi.tw.twks.server.resource.AssertionsResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertTrue;

public final class AssertionsResourceTest extends AbstractResourceTest {
    @Test
    public void testGetAssertions() throws Exception {
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

    @Override
    protected Object newResource(final Twks twks) {
        return new AssertionsResource(twks);
    }
}
