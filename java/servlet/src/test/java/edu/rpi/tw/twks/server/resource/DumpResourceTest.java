package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AbstractResourceTest;
import edu.rpi.tw.twks.test.ApisTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public final class DumpResourceTest extends AbstractResourceTest {
    @Test
    public void testPostDump() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final Response response =
                target()
                        .path("/dump")
                        .request()
                        .post(Entity.json(""));
        assertEquals(200, response.getStatus());
        ApisTest.checkDump(getTempDirPath());
    }

    @Override
    protected Object newResource(final Twks twks) {
        return new DumpResource(twks);
    }
}
