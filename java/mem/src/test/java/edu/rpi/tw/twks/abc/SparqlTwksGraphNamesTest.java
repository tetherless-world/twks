package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.mem.MemTwks;

public final class SparqlTwksGraphNamesTest extends TwksGraphNamesTest {
    @Override
    protected final MemTwks newTwks() {
        return new MemTwks();
    }
}
