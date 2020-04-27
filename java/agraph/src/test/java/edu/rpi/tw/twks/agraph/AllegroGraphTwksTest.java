package edu.rpi.tw.twks.agraph;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.test.TwksTest;

import java.nio.file.Path;

public final class AllegroGraphTwksTest extends TwksTest {
    final static String SERVER_URL = "http://twks-agraph:10035";

    @Override
    protected Twks newTwks(final Path dumpDirectoryPath) {
        return new AllegroGraphTwks(AllegroGraphTwksConfiguration.builder().setDumpDirectoryPath(dumpDirectoryPath).setServerUrl(SERVER_URL).build(), getMetricRegistry());
    }
}
