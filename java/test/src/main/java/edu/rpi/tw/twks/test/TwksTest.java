package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksConfiguration;

public abstract class TwksTest extends ApisTest<Twks> {
    @Override
    protected final Twks openSystemUnderTest() throws Exception {
        return newTwks(TwksConfiguration.builder().setDumpDirectoryPath(getTempDirPath().resolve("dump")).build());
    }

    @Override
    protected final void closeSystemUnderTest(final Twks sut) {
    }

    protected abstract Twks newTwks(TwksConfiguration configuration);
}
