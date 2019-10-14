package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.ServletContextTwks;

public abstract class AbstractResource {
    private final Twks twks;

    public AbstractResource() {
        this(ServletContextTwks.getInstance());
    }

    public AbstractResource(final Twks twks) {
        this.twks = twks;
    }

    protected final Twks getDb() {
        return twks;
    }
}
