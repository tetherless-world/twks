package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.api.Twks;

import java.nio.file.Path;

public abstract class TwksTest extends ApisTest<Twks> {
    @Override
    protected final void closeSystemUnderTest(final Twks sut) {
        sut.close();
    }

    protected abstract Twks newTwks(Path dumpDirectoryPath);

    @Override
    protected final Twks openSystemUnderTest() throws Exception {
        return newTwks(getTempDirPath().resolve("dump"));
    }
}
