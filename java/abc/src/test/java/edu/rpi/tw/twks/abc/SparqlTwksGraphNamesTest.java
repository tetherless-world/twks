package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.Twks;

public final class SparqlTwksGraphNamesTest extends TwksGraphNamesTest {
    @Override
    protected final TwksGraphNames newSystemUnderTest(final Twks twks) {
        return new SparqlTwksGraphNames(twks);
    }
}
