package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Twdb;

public final class Tdb2TwdbTest extends TwdbTest {
    @Override
    protected Twdb newTdb() {
        return new Tdb2Twdb();
    }
}
