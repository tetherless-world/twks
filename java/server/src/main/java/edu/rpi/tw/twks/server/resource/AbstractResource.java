package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.ServletTwks;

public abstract class AbstractResource {
    private final Twks twks;

    public AbstractResource() {
        this(ServletTwks.getInstance());
    }

    public AbstractResource(final Twks twks) {
        this.twks = twks;
    }

    protected final Twks getTwks() {
        return twks;
    }
}
