package edu.rpi.tw.twks.servlet.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.servlet.TwksServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResource {
    protected final Logger logger;
    private final Twks twks;

    public AbstractResource() {
        this(TwksServletContext.getInstance().getTwks());
    }

    public AbstractResource(final Twks twks) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.twks = twks;
    }

    protected final Twks getTwks() {
        return twks;
    }
}
