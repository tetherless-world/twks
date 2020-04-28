package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.configuration.TwksConfiguration;
import org.apache.jena.query.Dataset;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DatasetTwks<TwksConfigurationT extends TwksConfiguration, TwksMetricsT extends QuadStoreTwksMetrics> extends QuadStoreTwks<TwksConfigurationT, TwksMetricsT> {
    private final Dataset dataset;

    protected DatasetTwks(final TwksConfigurationT configuration, final Dataset dataset, final TwksMetricsT metrics) {
        super(configuration, metrics);
        this.dataset = checkNotNull(dataset);
    }

    final Dataset getDataset() {
        return dataset;
    }
}
