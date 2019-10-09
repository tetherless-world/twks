package edu.rpi.tw.twks.lib;

public final class Tdb2TwksTest extends TwksTest {
    @Override
    protected Twks newTdb() {
        return new Tdb2Twks();
    }
}
