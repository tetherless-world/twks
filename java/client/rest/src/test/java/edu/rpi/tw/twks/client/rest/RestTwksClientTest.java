package edu.rpi.tw.twks.client.rest;

import edu.rpi.tw.twks.test.TwksClientTest;

public final class RestTwksClientTest extends TwksClientTest<RestTwksClient> {
    @Override
    protected RestTwksClient openSystemUnderTest() throws Exception {
        return new RestTwksClient(RestTwksClientConfiguration.builder().setFromEnvironment().build());
    }
}
