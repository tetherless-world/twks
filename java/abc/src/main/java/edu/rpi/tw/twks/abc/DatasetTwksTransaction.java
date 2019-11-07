package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.nanopub.AutoCloseableIterable;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationFactory;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction extends AbstractTwksTransaction {
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwksTransaction.class);

    private final Dataset dataset;
    private final DatasetTransaction datasetTransaction;

    protected DatasetTwksTransaction(final TwksConfiguration configuration, final Dataset dataset, final ReadWrite readWrite) {
        super(configuration);
        this.dataset = checkNotNull(dataset);
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
    protected final void deleteNanopublication(final Set<String> nanopublicationGraphNames) {
        for (final String nanopublicationGraphName : nanopublicationGraphNames) {
            getDataset().removeNamedModel(nanopublicationGraphName);
        }
    }

    @Override
    protected final void getAssertions(final Set<String> assertionGraphNames, final Model assertions) {
        for (final String assertionGraphName : assertionGraphNames) {
            final Model assertion = getDataset().getNamedModel(assertionGraphName);
            assertions.add(assertion);
        }
    }

    protected final Dataset getDataset() {
        return dataset;
    }

    public final DatasetTransaction getDatasetTransaction() {
        return datasetTransaction;
    }

    @Override
    protected final AutoCloseableIterable<Nanopublication> iterateNanopublications() {
        return new NanopublicationFactory().iterateNanopublicationsFromDataset(getDataset(), getDatasetTransaction());
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri());
        nanopublication.toDataset(getDataset(), getDatasetTransaction());
        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return QueryExecutionFactory.create(query, getDataset());
    }
}
