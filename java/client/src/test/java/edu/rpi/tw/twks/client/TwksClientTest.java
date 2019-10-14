package edu.rpi.tw.twks.client;

import edu.rpi.tw.twks.test.ApisTest;

public final class TwksClientTest extends ApisTest<TwksClient> {
    @Override
    protected void closeSystemUnderTest(final TwksClient sut) {
    }

    @Override
    protected TwksClient openSystemUnderTest() throws Exception {
        return new TwksClient(new TwksClientConfiguration().setFromSystemProperties());
    }
}
