package edu.rpi.tw.twks.agraph;

import com.franz.agraph.jena.*;
import com.franz.agraph.repository.AGRepositoryConnection;
import edu.rpi.tw.twks.abc.QuadStoreTransaction;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.DoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

final class AllegroGraphQuadStoreTransaction implements QuadStoreTransaction {
    private final static Logger logger = LoggerFactory.getLogger(AllegroGraphTwksTransaction.class);
    private final AGGraphMaker graphMaker;
    private final AGRepositoryConnection repositoryConnection;

    AllegroGraphQuadStoreTransaction(final AGRepositoryConnection repositoryConnection) {
        graphMaker = new AGGraphMaker(repositoryConnection);
        this.repositoryConnection = checkNotNull(repositoryConnection);
        repositoryConnection.begin();
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
    public final boolean containsNamedGraph(final Uri graphName) {
        // Only includes graphs the client has seen. See the source.
        return graphMaker.hasGraph(graphName.toString());
    }

    @Override
    public final Model getNamedGraph(final Uri graphName) {
        final AGGraph graph = graphMaker.openGraph(graphName.toString(), true);
        return new AGModel(graph);
    }

    @Override
    public final Model getOrCreateNamedGraph(final Uri graphName) {
        final AGGraph graph = graphMaker.openGraph(graphName.toString(), false);
        return new AGModel(graph);
    }

    @Override
    public final QueryExecution query(final Query query) {
        return AGQueryExecutionFactory.create(AGQueryFactory.create(query.toString(Syntax.syntaxSPARQL_11)), new AGModel(graphMaker.createGraph()));
    }

    @Override
    public final void removeAllGraphs() {
        repositoryConnection.clear();
    }

    @Override
    public final void removeNamedGraph(final Uri graphName) {
        try {
            // Must open a graph locally before removing it
            graphMaker.openGraph(graphName.toString(), false);
            graphMaker.removeGraph(graphName.toString());
        } catch (final DoesNotExistException e) {
            logger.warn("tried to delete non-extant graph {}", graphName);
        }
    }
}
