package edu.rpi.tw.twks.factory;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.tdb.Tdb2Twks;
import edu.rpi.tw.twks.tdb.Tdb2TwksConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TwksFactory {
    private final static TwksFactory instance = new TwksFactory();
    private final static Logger logger = LoggerFactory.getLogger(TwksFactory.class);

    private TwksFactory() {
    }

    public static TwksFactory getInstance() {
        return instance;
    }

    public final Twks createTwks() {
        return createTwks(TwksFactoryConfiguration.DEFAULT, new MetricRegistry());
    }

    public final Twks createTwks(final TwksFactoryConfiguration configuration, final MetricRegistry metricRegistry) {
        if (configuration.getTdb2Configuration().isPresent()) {
            logger.info("using TDB2 configuration {}", configuration.getTdb2Configuration());
            return new Tdb2Twks(configuration.getTdb2Configuration().get(), metricRegistry);
        } else {
            logger.info("using memory-backed TDB2");
            return new Tdb2Twks(Tdb2TwksConfiguration.builder().build(), metricRegistry);
        }
    }
}
