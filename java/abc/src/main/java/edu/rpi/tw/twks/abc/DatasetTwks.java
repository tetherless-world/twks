package edu.rpi.tw.twks.abc;

import org.apache.jena.query.Dataset;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DatasetTwks extends AbstractTwks {
    private final Dataset dataset;

    protected DatasetTwks(final Dataset dataset) {
        this.dataset = checkNotNull(dataset);
    }

    @Override
    public void dump() {

    }

    protected final Dataset getDataset() {
        return dataset;
    }
}
