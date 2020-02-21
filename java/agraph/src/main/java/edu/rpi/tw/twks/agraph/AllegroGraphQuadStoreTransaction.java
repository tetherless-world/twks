package edu.rpi.tw.twks.agraph;

import com.franz.agraph.jena.*;
import com.franz.agraph.repository.AGRepositoryConnection;
import edu.rpi.tw.twks.abc.QuadStoreTransaction;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.DoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

final class AllegroGraphQuadStoreTransaction implements QuadStoreTransaction {
    private final static Logger logger = LoggerFactory.getLogger(AllegroGraphTwksTransaction.class);
    private final AGGraphMaker graphMaker;
    private final AGRepositoryConnection repositoryConnection;

    AllegroGraphQuadStoreTransaction(final AGRepositoryConnection repositoryConnection) {
        graphMaker = new AGGraphMaker(repositoryConnection);
        this.repositoryConnection = checkNotNull(repositoryConnection);
    }

    @Override
    public final void abort() {
        repositoryConnection.rollback();
    }

    @Override
    public final void addNamedGraph(final Uri graphName, final Model model) {
        final AGGraph agraphGraph = graphMaker.createGraph(graphName.toString(), true);
        final AGModel agraphModel = new AGModel(agraphGraph);
        agraphModel.add(model);
    }

    @Override
    public final void close() {
        graphMaker.close();
        repositoryConnection.close();
    }

    @Override
    public final void commit() {
        repositoryConnection.commit();
    }

    @Override
    public void deleteAllGraphs() {
        repositoryConnection.clear();
    }

    @Override
    public final void deleteNamedGraphs(final Set<Uri> graphNames) {
        for (final Uri graphName : graphNames) {
            try {
                // Must open a graph locally before removing it
                graphMaker.openGraph(graphName.toString(), false);
                graphMaker.removeGraph(graphName.toString());
            } catch (final DoesNotExistException e) {
                logger.warn("tried to delete non-extant graph {}", graphName);
            }
        }
    }

    final AGGraphMaker getGraphMaker() {
        return graphMaker;
    }

    @Override
    public final Model getNamedGraph(final Uri graphName) {
        final AGGraph graph = graphMaker.openGraph(graphName.toString(), false);
        return new AGModel(graph);
    }

    @Override
    public final Model getNamedGraphs(final Set<Uri> graphNames) {
        final Model result = ModelFactory.createDefaultModel();
        for (final Uri graphName : graphNames) {
            final AGGraph graph = graphMaker.openGraph(graphName.toString(), false);
            result.add(new AGModel(graph));
        }
        return result;
    }

    @Override
    public final boolean headNamedGraph(final Uri graphName) {
        // Only includes graphs the client has seen. See the source.
        return graphMaker.hasGraph(graphName.toString());
    }

    @Override
    public final QueryExecution query(final Query query) {
        return AGQueryExecutionFactory.create(AGQueryFactory.create(query.toString(Syntax.syntaxSPARQL_11)), new AGModel(graphMaker.createGraph()));
    }
}
