package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AbstractResourceTest;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public final class DumpResourceTest extends AbstractResourceTest {
    @Test
    public void testGetDump() {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final Response response =
                target()
                        .path("/dump")
                        .request()
                        .get();
        assertEquals(200, response.getStatus());
    }

    @Override
    protected Object newResource(final Twks twks) {
        return new AssertionsResource(twks);
    }
}
