package edu.rpi.tw.twks.mem;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.test.TwksTransactionTest;

import java.nio.file.Path;

public final class MemTwksTransactionTest extends TwksTransactionTest {
    @Override
    protected Twks newTwks(final Path dumpDirectoryPath) {
        return new MemTwks(MemTwksConfiguration.builder().setDumpDirectoryPath(dumpDirectoryPath).build(), getMetricRegistry());
    }
}
