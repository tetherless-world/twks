package edu.rpi.tw.twdb.server.resource;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.ServletContextTwdb;

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
