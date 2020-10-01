package edu.rpi.tw.twks.tdb;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.abc.DatasetTwks;
import edu.rpi.tw.twks.abc.QuadStoreTwksMetrics;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

public final class Tdb2Twks extends DatasetTwks<Tdb2TwksConfiguration, QuadStoreTwksMetrics> {
    public Tdb2Twks(final Tdb2TwksConfiguration configuration, final MetricRegistry metricRegistry) {
        this(configuration,
                Tdb2DatasetFactory.getInstance().createTdb2Dataset(configuration),
                metricRegistry
        );
    }

    public Tdb2Twks(final Tdb2TwksConfiguration configuration, final Dataset dataset, final MetricRegistry metricRegistry) {
        super(configuration, dataset, new QuadStoreTwksMetrics(metricRegistry));
    }

    @Override
    protected final TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        return new Tdb2TwksTransaction(readWrite, this);
    }
}
