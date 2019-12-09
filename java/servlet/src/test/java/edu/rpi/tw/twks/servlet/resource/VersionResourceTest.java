package edu.rpi.tw.twks.servlet.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.rpi.tw.twks.api.TwksVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class VersionResourceTest extends AbstractResourceTest {
    @Test
    public void testGetVersion() throws Exception {
        getTwks().putNanopublication(getTestData().specNanopublication);
        final TwksVersion actual =
                target()
                        .path("/version")
                        .request()
                        .get(JsonTwksVersion.class);
        assertEquals(getTwks().getVersion(), actual);
    }

    private final static class JsonTwksVersion extends TwksVersion {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public JsonTwksVersion(@JsonProperty("incremental") final int incremental, @JsonProperty("major") final int major, @JsonProperty("minor") final int minor) {
            super(incremental, major, minor);
        }
    }
}
