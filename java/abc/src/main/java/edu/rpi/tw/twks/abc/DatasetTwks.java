package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.configuration.TwksConfiguration;
import org.apache.jena.query.Dataset;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DatasetTwks<TwksConfigurationT extends TwksConfiguration, TwksMetricsT extends QuadStoreTwksMetrics> extends QuadStoreTwks<TwksConfigurationT, TwksMetricsT> {
    private final Dataset dataset;

    protected DatasetTwks(final TwksConfigurationT configuration, final Dataset dataset, final TwksMetricsT metrics) {
        super(configuration, metrics);
        this.dataset = checkNotNull(dataset);
    }

    @Override
    public void close() throws IOException {
        dataset.close();
    }

    final Dataset getDataset() {
        return dataset;
    }
}
