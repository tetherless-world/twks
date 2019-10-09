package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.lib.Twks;
import edu.rpi.tw.twks.server.ServletContextTwks;

public abstract class AbstractResource {
    private final Twks db;

    public AbstractResource() {
        this(ServletContextTwks.getInstance());
    }

    public AbstractResource(final Twks db) {
        this.db = db;
    }

    protected final Twks getDb() {
        return db;
    }
}
