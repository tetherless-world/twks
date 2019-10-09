package edu.rpi.tw.twks.lib;

import edu.rpi.tw.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
final class DatasetTwksTransaction implements TwksTransaction {
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
