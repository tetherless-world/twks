package edu.rpi.tw.twks.mem;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.abc.DatasetTwks;
import edu.rpi.tw.twks.abc.QuadStoreTwksMetrics;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;

/**
 * A Twks implementation backed by the default in-memory Dataset.
 */
public final class MemTwks extends DatasetTwks<MemTwksConfiguration, QuadStoreTwksMetrics> {
    public MemTwks(final MemTwksConfiguration configuration, final MetricRegistry metricRegistry) {
        super(configuration, DatasetFactory.createTxnMem(), new QuadStoreTwksMetrics(metricRegistry));
    }

    @Override
    protected final TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        return new MemTwksTransaction(readWrite, this);
    }
}
