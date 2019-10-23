package edu.rpi.tw.twks.abc;

import org.apache.jena.query.Dataset;

import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DatasetTwks extends AbstractTwks {
    private final Dataset dataset;

    protected DatasetTwks(final Dataset dataset, final Path dumpDirectoryPath) {
        this.dataset = checkNotNull(dataset);
        this.dumpDirectoryPath = checkNotNull(dumpDirectoryPath);
    }

    @Override
    public void dump() {

    }

    protected final Dataset getDataset() {
        return dataset;
    }
}
