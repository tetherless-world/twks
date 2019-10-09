package edu.rpi.tw.twks.lib;

import edu.rpi.tw.twks.api.Twks;

public final class Tdb2TwksTest extends TwksTest {
    @Override
    protected Twks newTdb() {
        return new Tdb2Twks();
    }
}
