package edu.rpi.tw.twks.client;

public final class RestTwksClientTest extends TwksClientTest<RestTwksClient> {
    @Override
    protected RestTwksClient openSystemUnderTest() throws Exception {
        return new RestTwksClient(RestTwksClientConfiguration.builder().setFromSystemProperties().build());
    }
}
