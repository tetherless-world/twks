package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.nanopub.AutoCloseableIterable;
import edu.rpi.tw.twks.nanopub.DatasetNanopublications;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction<TwksT extends DatasetTwks<?>> extends QuadStoreTwksTransaction<TwksT> {
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwksTransaction.class);

    private final Dataset dataset;
    private final DatasetTransaction datasetTransaction;

    protected DatasetTwksTransaction(final ReadWrite readWrite, final TwksT twks) {
        super(new DatasetQuadStore(twks.getDataset(), new DatasetTransaction(twks.getDataset(), readWrite)), twks);
        this.dataset = twks.getDataset();
        this.datasetTransaction = new DatasetTransaction(dataset, readWrite);
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

    protected final Dataset getDataset() {
        return dataset;
    }

    public final DatasetTransaction getDatasetTransaction() {
        return datasetTransaction;
    }

    @Override
    protected final AutoCloseableIterable<Nanopublication> iterateNanopublications() {
        return new DatasetNanopublications(getDataset(), getDatasetTransaction());
    }
}

