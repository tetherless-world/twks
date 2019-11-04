package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;
import org.apache.jena.query.Dataset;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DatasetTwks<TwksConfigurationT extends TwksConfiguration> extends AbstractTwks<TwksConfigurationT> {
    private final Dataset dataset;

    protected DatasetTwks(final TwksConfigurationT configuration, final Dataset dataset) {
        super(configuration);
        this.dataset = checkNotNull(dataset);
    }

    protected final Dataset getDataset() {
        return dataset;
    }
}
