package edu.rpi.tw.twks.agraph;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.test.TwksTransactionTest;

public final class AllegroGraphTwksTransactionTest extends TwksTransactionTest {
    @Override
    protected Twks newTwks(final TwksConfiguration configuration) {
        return new AllegroGraphTwks(configuration);
    }
}
