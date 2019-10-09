package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.ServletContextTwdb;

public abstract class AbstractResource {
    private final Twks db;

    public AbstractResource() {
        this(ServletContextTwdb.getInstance());
    }

    public AbstractResource(final Twks db) {
        this.db = db;
    }

    protected final Twks getDb() {
        return db;
    }
}
