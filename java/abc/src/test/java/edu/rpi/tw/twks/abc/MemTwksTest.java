package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.test.TwksTest;

import java.nio.file.Path;

public final class MemTwksTest extends TwksTest {
    @Override
    protected Twks newTwks(final Path dumpDirectoryPath) {
        return new MemTwks(TwksConfiguration.builder().setDumpDirectoryPath(dumpDirectoryPath).build());
    }
}
