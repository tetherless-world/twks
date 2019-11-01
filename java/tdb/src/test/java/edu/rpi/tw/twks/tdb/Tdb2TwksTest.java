package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.test.TwksTest;

import java.nio.file.Path;

public final class Tdb2TwksTest extends TwksTest {
    @Override
    protected Twks newTwks(final Path dumpDirectoryPath) {
        return new Tdb2Twks(Tdb2TwksConfiguration.builder().setDumpDirectoryPath(dumpDirectoryPath).build());
    }
}
