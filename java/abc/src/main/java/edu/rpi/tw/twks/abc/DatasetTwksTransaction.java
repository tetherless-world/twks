package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.nanopub.AutoCloseableIterable;
import edu.rpi.tw.twks.nanopub.DatasetNanopublications;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction<TwksT extends Twks> extends AbstractTwksTransaction<TwksT> {
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwksTransaction.class);

    private final Dataset dataset;
    private final DatasetTransaction datasetTransaction;

    protected DatasetTwksTransaction(final Dataset dataset, final ReadWrite readWrite, final TwksT twks) {
        super(twks);
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
    public final void deleteNanopublications() {
        final ImmutableList<String> datasetNames = ImmutableList.copyOf(getDataset().listNames());
        for (final String name : datasetNames) {
            dataset.removeNamedModel(name);
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
        return new DatasetNanopublications(getDataset(), getDatasetTransaction());
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
