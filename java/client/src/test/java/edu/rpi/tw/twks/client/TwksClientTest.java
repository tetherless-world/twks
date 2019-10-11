package edu.rpi.tw.twks.client;

import edu.rpi.tw.twks.test.ApisTest;

public final class TwksClientTest extends ApisTest<TwksClient> {
    @Override
    protected void closeSystemUnderTest(final TwksClient sut) {
    }

    @Override
    protected TwksClient openSystemUnderTest() throws Exception {
        final String baseUrlPropertyName = getClass().getPackage().getName() + "." + getClass().getSimpleName() + ".baseUrl";
        final String baseUrl = System.getProperty(baseUrlPropertyName);
        return baseUrl != null && !baseUrl.isEmpty() ? new TwksClient(baseUrl) : new TwksClient();
    }
}
