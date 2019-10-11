package edu.rpi.tw.twks.core;

import edu.rpi.tw.twks.test.ApisTest;

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
