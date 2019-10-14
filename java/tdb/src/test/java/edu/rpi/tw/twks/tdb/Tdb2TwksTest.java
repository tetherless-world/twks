package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.core.Twks;

public final class Tdb2TwksTest extends TwksTest {
    @Override
    protected Twks newTwks() {
        return new Tdb2Twks();
    }
}
