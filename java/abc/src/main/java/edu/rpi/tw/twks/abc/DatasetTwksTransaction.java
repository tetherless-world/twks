package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.nanopub.AutoCloseableIterable;
import edu.rpi.tw.twks.nanopub.DatasetNanopublications;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction<TwksT extends DatasetTwks<?>> extends AbstractTwksTransaction<TwksT> {
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwksTransaction.class);

    private final Dataset dataset;
    private final DatasetTransaction datasetTransaction;

    protected DatasetTwksTransaction(final ReadWrite readWrite, final TwksT twks) {
        super(twks.getGraphNames(), twks);
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

    @Override
    protected final void deleteAllGraphs() {
        final ImmutableList<String> datasetNames = ImmutableList.copyOf(getDataset().listNames());
        for (final String name : datasetNames) {
            dataset.removeNamedModel(name);
        }
    }

    @Override
    protected final void deleteNamedGraphs(final Set<Uri> graphNames) {
        for (final Uri nanopublicationGraphName : graphNames) {
            getDataset().removeNamedModel(nanopublicationGraphName.toString());
        }
    }

    protected final Dataset getDataset() {
        return dataset;
    }

    public final DatasetTransaction getDatasetTransaction() {
        return datasetTransaction;
    }

    @Override
    protected final void getNamedGraphs(final Set<Uri> graphNames, final Model intoModel) {
        for (final Uri assertionGraphName : graphNames) {
            final Model assertion = getDataset().getNamedModel(assertionGraphName.toString());
            intoModel.add(assertion);
        }
    }

    @Override
    protected final AutoCloseableIterable<Nanopublication> iterateNanopublications() {
        return new DatasetNanopublications(getDataset(), getDatasetTransaction());
    }

    @Override
    protected final PutNanopublicationResult putNanopublicationImpl(final Nanopublication nanopublication) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri());
        switch (deleteResult) {
            case DELETED:
                logger.debug("deleted nanopublication {} before put", nanopublication.getUri());
                break;
            case NOT_FOUND:
                logger.debug("nanopublication {} did not exist before put", nanopublication.getUri());
                break;
        }
        nanopublication.toDataset(getDataset(), getDatasetTransaction());
        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return QueryExecutionFactory.create(query, getDataset());
    }
}

