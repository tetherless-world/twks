package edu.rpi.tw.twks.factory;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.agraph.AllegroGraphTwks;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.tdb.Tdb2DatasetFactory;
import edu.rpi.tw.twks.tdb.Tdb2Twks;
import edu.rpi.tw.twks.tdb.Tdb2TwksConfiguration;
import edu.rpi.tw.twks.text.FullTextSearchableDatasetFactory;
import org.apache.jena.query.Dataset;
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
        if (configuration.getAllegroGraphConfiguration().isPresent()) {
            logger.info("using AllegroGraph configuration {}", configuration.getAllegroGraphConfiguration().get());
            return new AllegroGraphTwks(configuration.getAllegroGraphConfiguration().get(), metricRegistry);
        }

        final Tdb2TwksConfiguration tdb2Configuration;
        if (configuration.getTdb2Configuration().isPresent()) {
            logger.info("using TDB2 configuration {}", configuration.getTdb2Configuration().get());
            tdb2Configuration = configuration.getTdb2Configuration().get();
        } else {
            logger.info("using memory-backed TDB2");
            tdb2Configuration = Tdb2TwksConfiguration.builder().build();
        }

        Dataset dataset = Tdb2DatasetFactory.getInstance().createTdb2Dataset(tdb2Configuration);

        if (configuration.getFullTextSearchConfiguration().isPresent()) {
            logger.info("enabling full-text search with configuration {}", configuration.getFullTextSearchConfiguration().get());
            dataset = FullTextSearchableDatasetFactory.getInstance().createFullTextSearchableDataset(configuration.getFullTextSearchConfiguration().get(), dataset);
        }

        return new Tdb2Twks(tdb2Configuration, dataset, metricRegistry);
    }
}
