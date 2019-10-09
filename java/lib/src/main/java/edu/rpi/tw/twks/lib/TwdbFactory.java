package edu.rpi.tw.twks.lib;

import edu.rpi.tw.twks.api.Twks;
import org.apache.jena.dboe.base.file.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TwdbFactory {
    private final static Logger logger = LoggerFactory.getLogger(TwdbFactory.class);
    private final static TwdbFactory instance = new TwdbFactory();

    private TwdbFactory() {
    }

    public static TwdbFactory getInstance() {
        return instance;
    }

    public final Twks createTwdb() {
        return createTwdb(new TwdbConfiguration());
    }

    public final Twks createTwdb(final TwdbConfiguration configuration) {
        if (configuration.getTdb2Location().isPresent()) {
            logger.info("using TDB2 at {}", configuration.getTdb2Location().get());
            return new Tdb2Twks(Location.create(configuration.getTdb2Location().get()));
        } else {
            logger.info("using memory-backed TDB2");
            return new Tdb2Twks(Location.mem());
        }
    }
}
