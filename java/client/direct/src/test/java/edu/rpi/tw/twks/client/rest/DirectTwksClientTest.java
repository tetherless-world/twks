package edu.rpi.tw.twks.client.rest;

import edu.rpi.tw.twks.client.direct.DirectTwksClient;
import edu.rpi.tw.twks.mem.MemTwks;
import edu.rpi.tw.twks.mem.MemTwksConfiguration;
import edu.rpi.tw.twks.test.TwksClientTest;

public final class DirectTwksClientTest extends TwksClientTest<DirectTwksClient> {
    @Override
    protected DirectTwksClient openSystemUnderTest() throws Exception {
        return new DirectTwksClient(new MemTwks(MemTwksConfiguration.builder().build()));
    }

    @Override
    public final void testDump() throws Exception {
    }
}
