package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DatasetQuadStoreTransaction implements QuadStoreTransaction {
    private final Dataset dataset;
    private final DatasetTransaction datasetTransaction;

    public DatasetQuadStoreTransaction(final Dataset dataset, final DatasetTransaction datasetTransaction) {
        this.dataset = checkNotNull(dataset);
        this.datasetTransaction = checkNotNull(datasetTransaction);
    }

    @Override
    public final void abort() {
        datasetTransaction.abort();
    }

    @Override
    public final void addNamedGraph(final Uri graphName, final Model model) {
        dataset.addNamedModel(graphName.toString(), model);
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
    public final boolean containsNamedGraph(final Uri graphName) {
        return dataset.containsNamedModel(graphName.toString());
    }

    @Override
    public final void deleteAllGraphs() {
        final ImmutableList<String> datasetNames = ImmutableList.copyOf(dataset.listNames());
        for (final String name : datasetNames) {
            dataset.removeNamedModel(name);
        }
    }

    @Override
    public void deleteNamedGraph(final Uri graphName) {
        dataset.removeNamedModel(graphName.toString());
    }

    @Override
    public final Model getNamedGraph(final Uri graphName) {
        return dataset.getNamedModel(graphName.toString());
    }

    @Override
    public final QueryExecution query(final Query query) {
        return QueryExecutionFactory.create(query, dataset);
    }
}
