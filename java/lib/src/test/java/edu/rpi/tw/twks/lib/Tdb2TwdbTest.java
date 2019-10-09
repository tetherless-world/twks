package edu.rpi.tw.twks.lib;

import edu.rpi.tw.twks.api.Twdb;

public final class Tdb2TwdbTest extends TwdbTest {
    @Override
    protected Twdb newTdb() {
        return new Tdb2Twdb();
    }
}
