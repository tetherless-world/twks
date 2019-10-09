package edu.rpi.tw.twks.lib;

import edu.rpi.tw.twks.api.Twdb;
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

    public final Twdb createTwdb() {
        return createTwdb(new TwdbConfiguration());
    }

    public final Twdb createTwdb(final TwdbConfiguration configuration) {
        if (configuration.getTdb2Location().isPresent()) {
            logger.info("using TDB2 at {}", configuration.getTdb2Location().get());
            return new Tdb2Twdb(Location.create(configuration.getTdb2Location().get()));
        } else {
            logger.info("using memory-backed TDB2");
            return new Tdb2Twdb(Location.mem());
        }
    }
}
