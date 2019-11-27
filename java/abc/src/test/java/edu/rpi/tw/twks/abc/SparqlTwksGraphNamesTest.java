package edu.rpi.tw.twks.abc;

public final class SparqlTwksGraphNamesTest extends TwksGraphNamesTest {
    @Override
    protected final MemTwks newTwks() {
        return new MemTwks();
    }
}
