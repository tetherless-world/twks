package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.core.Twks;

public final class Tdb2TwksTransactionTest extends TwksTransactionTest {
    @Override
    protected Twks newTwks() {
        return new Tdb2Twks();
    }
}
