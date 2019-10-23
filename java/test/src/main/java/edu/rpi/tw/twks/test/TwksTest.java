package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.api.Twks;

public abstract class TwksTest extends ApisTest<Twks> {
    @Override
    protected final Twks openSystemUnderTest() throws Exception {
        return newTwks();
    }

    @Override
    protected final void closeSystemUnderTest(final Twks sut) {
    }

    protected abstract Twks newTwks();
}
