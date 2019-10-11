package edu.rpi.tw.twks.core;

import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
abstract class DatasetTwksTransaction implements TwksTransaction {
    private final DatasetTransaction datasetTransaction;

    DatasetTwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        this.datasetTransaction = new DatasetTransaction(dataset, readWrite);
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
