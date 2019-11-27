package edu.rpi.tw.twks.abc;

public final class SparqlTwksGraphNamesTest extends TwksGraphNamesTest {
    @Override
    protected final TwksGraphNames newSystemUnderTest() {
        return new SparqlTwksGraphNames();
    }
}
