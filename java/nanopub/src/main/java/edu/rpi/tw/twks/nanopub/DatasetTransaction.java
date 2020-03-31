package edu.rpi.tw.twks.nanopub;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DatasetTransaction implements AutoCloseable {
    private final Dataset dataset;

    public DatasetTransaction(final Dataset dataset, final ReadWrite readWrite) {
        this.dataset = checkNotNull(dataset);
        if (dataset.supportsTransactions()) {
            dataset.begin(readWrite);
        }
    }

    public final void abort() {
        if (dataset.supportsTransactionAbort()) {
            dataset.abort();
        }
    }

    @Override
    public final void close() {
        if (dataset.supportsTransactions()) {
            dataset.end();
        }
    }

    public final void commit() {
        if (dataset.supportsTransactions()) {
            dataset.commit();
        }
    }

    public final Dataset getDataset() {
        return dataset;
    }
}
