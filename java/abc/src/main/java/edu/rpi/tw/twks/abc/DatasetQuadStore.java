package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DatasetQuadStore implements QuadStore {
    private final Dataset dataset;

    public DatasetQuadStore(final Dataset dataset, final DatasetTransaction transaction) {
        this.dataset = checkNotNull(dataset);
    }

    @Override
    public final void addNamedGraph(final Uri graphName, final Model model) {
        dataset.addNamedModel(graphName.toString(), model);
    }

    @Override
    public final void deleteAllGraphs() {
        final ImmutableList<String> datasetNames = ImmutableList.copyOf(dataset.listNames());
        for (final String name : datasetNames) {
            dataset.removeNamedModel(name);
        }
    }

    @Override
    public final void deleteNamedGraphs(final Set<Uri> graphNames) {
        for (final Uri nanopublicationGraphName : graphNames) {
            dataset.removeNamedModel(nanopublicationGraphName.toString());
        }
    }

    @Override
    public final Model getNamedGraph(final Uri graphName) {
        return dataset.getNamedModel(graphName.toString());
    }

    @Override
    public final Model getNamedGraphs(final Set<Uri> graphNames) {
        final Model result = ModelFactory.createDefaultModel();
        for (final Uri graphName : graphNames) {
            result.add(dataset.getNamedModel(graphName.toString()));
        }
        return result;
    }

    @Override
    public final boolean headNamedGraph(final Uri graphName) {
        return dataset.containsNamedModel(graphName.toString());
    }

    @Override
    public final QueryExecution query(final Query query) {
        return QueryExecutionFactory.create(query, dataset);
    }
}
