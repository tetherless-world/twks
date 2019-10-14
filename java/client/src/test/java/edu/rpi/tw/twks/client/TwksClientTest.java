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
        if (baseUrl != null) {
            System.out.println("Using baseUrl from property: " + baseUrl);
            return new TwksClient(baseUrl);
        } else {
            System.out.println(baseUrlPropertyName + " not set, using default baseUrl");
            return new TwksClient();
        }
    }
}
