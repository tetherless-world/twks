package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Twdb;

public final class TwdbImplTest extends TwdbTest {
    @Override
    protected Twdb newTdb() {
        return new TwdbImpl();
    }
}
