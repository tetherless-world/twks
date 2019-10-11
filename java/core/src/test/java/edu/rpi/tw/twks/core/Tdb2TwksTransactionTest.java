package edu.rpi.tw.twks.core;

public final class Tdb2TwksTransactionTest extends TwksTransactionTest {
    @Override
    protected Twks newTwks() {
        return new Tdb2Twks();
    }
}
