package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.DatasetTransaction;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

/**
 * A TwdbTransaction that wraps a DatasetTransaction.
 */
final class DatasetTwdbTransaction implements TwdbTransaction {
    private final DatasetTransaction datasetTransaction;

    DatasetTwdbTransaction(final Dataset dataset, final ReadWrite readWrite) {
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
