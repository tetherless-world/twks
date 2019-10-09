package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twdb;
import edu.rpi.tw.twks.server.ServletContextTwdb;

public abstract class AbstractResource {
    private final Twdb db;

    public AbstractResource() {
        this(ServletContextTwdb.getInstance());
    }

    public AbstractResource(final Twdb db) {
        this.db = db;
    }

    protected final Twdb getDb() {
        return db;
    }
}
