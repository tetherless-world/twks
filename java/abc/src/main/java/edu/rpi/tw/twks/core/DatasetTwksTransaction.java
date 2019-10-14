package edu.rpi.tw.twks.core;

import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction implements TwksTransaction {
    private final DatasetTransaction datasetTransaction;
    private final Dataset dataset;

    protected DatasetTwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        this.dataset = checkNotNull(dataset);
        this.datasetTransaction = new DatasetTransaction(dataset, readWrite);
    }

    protected final Dataset getDataset() {
        return dataset;
    }

    public final DatasetTransaction getDatasetTransaction() {
        return datasetTransaction;
    }

    @Override
    public final void abort() {
        datasetTransaction.abort();
    }

    @Override
    public final void close() {
        datasetTransaction.close();
    }

    @Override
    public final void commit() {
        datasetTransaction.commit();
    }
}
