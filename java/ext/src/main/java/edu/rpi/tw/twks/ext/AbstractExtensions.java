package edu.rpi.tw.twks.ext;

import edu.rpi.tw.twks.api.Twks;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class AbstractExtensions {
    private final Twks twks;

    protected AbstractExtensions(final Twks twks) {
        this.twks = checkNotNull(twks);
    }

    public abstract void destroy();

    public abstract void initialize();

    protected final Twks getTwks() {
        return twks;
    }
}
