package edu.rpi.tw.twks.factory;

import org.apache.jena.dboe.base.file.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TwksFactory {
    private final static Logger logger = LoggerFactory.getLogger(TwksFactory.class);
    private final static TwksFactory instance = new TwksFactory();

    private TwksFactory() {
    }

    public static TwksFactory getInstance() {
        return instance;
    }

    public final Twks createTwks() {
        return createTwks(new TwksConfiguration());
    }

    public final Twks createTwks(final TwksConfiguration configuration) {
        if (configuration.getTdb2Location().isPresent()) {
            logger.info("using TDB2 at {}", configuration.getTdb2Location().get());
            return new Tdb2Twks(Location.create(configuration.getTdb2Location().get()));
        } else {
            logger.info("using memory-backed TDB2");
            return new Tdb2Twks(Location.mem());
        }
    }
}
