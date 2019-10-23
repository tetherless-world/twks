package edu.rpi.tw.twks.ext;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksExtension;

public final class TestTwksExtension implements TwksExtension {
    static boolean destroyed = false;
    static boolean instantiated = false;
    static boolean initialized = false;

    public TestTwksExtension() {
        instantiated = true;
    }

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    public void initialize(final Twks twks) {
        initialized = true;
    }
}
