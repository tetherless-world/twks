package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.api.Twks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class TwksServletContext {
    private final static Logger logger = LoggerFactory.getLogger(TwksServletContext.class);
    private static TwksServletContext instance = null;
    private final Twks twks;

    private TwksServletContext(final Twks twks) {
        this.twks = checkNotNull(twks);
    }

    public synchronized static void destroyInstance() {
        if (instance != null) {
            instance.destroy();
        }
    }

    public final synchronized static TwksServletContext getInstance() {
        return checkNotNull(instance);
    }

    public synchronized static void initializeInstance(final Twks twks) {
        checkState(instance == null);
        instance = new TwksServletContext(twks);
    }

    private void destroy() {
    }

    public final Twks getTwks() {
        return twks;
    }
}
