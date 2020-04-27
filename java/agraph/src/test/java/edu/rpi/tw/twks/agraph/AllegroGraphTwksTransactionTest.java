package edu.rpi.tw.twks.agraph;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.test.TwksTransactionTest;

import java.nio.file.Path;

public final class AllegroGraphTwksTransactionTest extends TwksTransactionTest {
    @Override
    protected Twks newTwks(final Path dumpDirectoryPath) {
        return new AllegroGraphTwks(AllegroGraphTwksConfiguration.builder().setDumpDirectoryPath(dumpDirectoryPath).setServerUrl(AllegroGraphTwksTest.SERVER_URL).build(), getMetricRegistry());
    }
}
