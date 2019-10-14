package edu.rpi.tw.twks.server.resource.assertions;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AbstractResourceTest;
import org.apache.jena.riot.Lang;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class AssertionsResourceTest extends AbstractResourceTest {
    @Test
    public void testGetAssertions() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final String responseBody =
                target()
                        .path("/assertions")
                        .request(Lang.TRIG.getContentType().getContentType())
                        .get(String.class);
        assertEquals(toTrigString(getTestData().specNanopublication.getAssertion().getModel()), responseBody);
    }

    @Override
    protected Object newResource(final Twks twks) {
        return new AssertionsResource(twks);
    }
}
