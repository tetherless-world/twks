package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class TwksClientTest<TwksClientT extends TwksClient> extends ApisTest<TwksClientT> {
    @Override
    protected void closeSystemUnderTest(final TwksClientT sut) {
    }

    @Override
    public void testDump() throws Exception {
        getSystemUnderTest().putNanopublication(getTestData().specNanopublication);

        getSystemUnderTest().dump();

        // Can't verify the dump happened from the client side
    }

    @Test
    public final void testGetClientVersion() {
        assertEquals(TwksLibraryVersion.getInstance(), getSystemUnderTest().getClientVersion());
    }

    @Test
    public void testGetServerVersion() {
        assertEquals(TwksLibraryVersion.getInstance(), getSystemUnderTest().getServerVersion());
    }
}
