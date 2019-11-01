package edu.rpi.tw.twks.agraph;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.test.TwksTest;

public final class AllegroGraphTwksTest extends TwksTest {
    @Override
    protected Twks newTwks(final TwksConfiguration configuration) {
        return new AllegroGraphTwks(configuration);
    }
}
