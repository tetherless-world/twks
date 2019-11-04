package edu.rpi.tw.twks.agraph;

import com.franz.agraph.jena.*;
import com.franz.agraph.repository.AGRepositoryConnection;
import edu.rpi.tw.twks.abc.AbstractTwksTransaction;
import edu.rpi.tw.twks.nanopub.AutoCloseableIterable;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.DoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

final class AllegroGraphTwksTransaction extends AbstractTwksTransaction {
    private final static Logger logger = LoggerFactory.getLogger(AllegroGraphTwksTransaction.class);
    private final AGGraphMaker graphMaker;
    private final AGRepositoryConnection repositoryConnection;

    public AllegroGraphTwksTransaction(final AllegroGraphTwksConfiguration configuration, final AGRepositoryConnection repositoryConnection) {
        super(configuration);
        this.repositoryConnection = checkNotNull(repositoryConnection);
        graphMaker = new AGGraphMaker(repositoryConnection);
    }

    @Override
    public void abort() {
        // TODO: throw away observed operations
    }

    @Override
    public void close() {
        graphMaker.close();
        repositoryConnection.close();
    }

    @Override
    public void commit() {
        // TODO: execute observed operations
    }

    @Override
    protected void deleteNanopublication(final Set<String> nanopublicationGraphNames) {
        for (final String graphName : nanopublicationGraphNames) {
            try {
                // Must open a graph locally before removing it
                graphMaker.openGraph(graphName, false);
                graphMaker.removeGraph(graphName);
            } catch (final DoesNotExistException e) {
                logger.warn("tried to delete non-extant graph {}", graphName);
            }
        }
    }

    @Override
    protected final void getAssertions(final Set<String> assertionGraphNames, final Model assertions) {
        for (final String graphName : assertionGraphNames) {
            final AGGraph graph = graphMaker.openGraph(graphName, false);
            assertions.add(new AGModel(graph));
        }
    }

    @Override
    protected AutoCloseableIterable<Nanopublication> iterateNanopublications() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri());

        final Dataset nanopublicationDataset = nanopublication.toDataset();
        for (final Iterator<String> nameI = nanopublicationDataset.listNames(); nameI.hasNext(); ) {
            final String nanopublicationPartName = nameI.next();
            final Model nanopublicationPartModel = nanopublicationDataset.getNamedModel(nanopublicationPartName);
            final AGGraph agraphGraph = graphMaker.createGraph(nanopublicationPartName, true);
            final AGModel agraphModel = new AGModel(agraphGraph);
            agraphModel.add(nanopublicationPartModel);
        }

        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public QueryExecution queryNanopublications(final Query query) {
        final AGQuery agraphQuery = AGQueryFactory.create(query.toString(Syntax.syntaxSPARQL_11));
        return AGQueryExecutionFactory.create(agraphQuery, new AGModel(graphMaker.createGraph()));
    }
}
