package edu.rpi.tw.twks.examples.extcp;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksExtension;

public final class TwksExtensionExample implements TwksExtension {
    @Override
    public final void destroy() {
    }

    @Override
    public final void initialize(final Twks twks) {
        System.out.println("Initializing example extension");
        System.out.println("Initialized example extension");
    }
}
