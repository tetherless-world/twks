package edu.rpi.tw.twks.agraph;

import com.franz.agraph.jena.*;
import com.franz.agraph.repository.AGRepositoryConnection;
import edu.rpi.tw.twks.abc.AbstractTwksTransaction;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.DoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

final class AllegroGraphTwksTransaction extends AbstractTwksTransaction {
    private final static String ITERATE_NANOPUBLICATIONS_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A ?H ?I ?np ?P where {\n" +
            "graph ?H {\n" +
            "  ?np a np:Nanopublication .\n" +
            "  ?np np:hasAssertion ?A .\n" +
            "  ?np np:hasProvenance ?P .\n" +
            "  ?np np:hasPublicationInfo ?I .\n" +
            "}}";
    private final static AGQuery ITERATE_NANOPUBLICATIONS_QUERY = AGQueryFactory.create(ITERATE_NANOPUBLICATIONS_QUERY_STRING);

    private final static Logger logger = LoggerFactory.getLogger(AllegroGraphTwksTransaction.class);
    private final AGGraphMaker graphMaker;
    private final AGRepositoryConnection repositoryConnection;

    public AllegroGraphTwksTransaction(final AllegroGraphTwksConfiguration configuration, final AGRepositoryConnection repositoryConnection) {
        super(configuration);
        this.repositoryConnection = checkNotNull(repositoryConnection);
        graphMaker = new AGGraphMaker(repositoryConnection);
        repositoryConnection.begin();
    }

    @Override
    public void abort() {
        repositoryConnection.rollback();
    }

    @Override
    public void close() {
        graphMaker.close();
        repositoryConnection.close();
    }

    @Override
    public void commit() {
        repositoryConnection.commit();
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
        return new AutoCloseableIterable<Nanopublication>() {
            private @Nullable
            QueryExecution queryExecution = null;

            @Override
            public void close() {
                if (queryExecution != null) {
                    queryExecution.close();
                }
            }

            @Override
            public Iterator<Nanopublication> iterator() {
                if (queryExecution != null) {
                    queryExecution.close();
                }
                queryExecution = queryNanopublications(ITERATE_NANOPUBLICATIONS_QUERY);
                final ResultSet resultSet = queryExecution.execSelect();

                return new Iterator<Nanopublication>() {
                    private NanopublicationPart getNanopublicationPart(final String nanopublicationPartName) {
                        final AGGraph graph = graphMaker.openGraph(nanopublicationPartName, false);
                        final AGModel model = new AGModel(graph);
                        return new NanopublicationPart(model, Uri.parse(nanopublicationPartName));
                    }

                    @Override
                    public boolean hasNext() {
                        return resultSet.hasNext();
                    }

                    @Override
                    public Nanopublication next() {
                        final QuerySolution querySolution = resultSet.nextSolution();
                        final String nanopublicationAssertionGraphName = querySolution.getResource("A").getURI();
                        final String nanopublicationHeadGraphName = querySolution.getResource("H").getURI();
                        final String nanopublicationProvenanceGraphName = querySolution.getResource("P").getURI();
                        final String nanopublicationPublicationInfoGraphName = querySolution.getResource("I").getURI();
                        final String nanopublicationUri = querySolution.getResource("np").getURI();

                        try {
                            return SpecificationNanopublicationDialect.createNanopublicationFromParts(
                                    getNanopublicationPart(nanopublicationAssertionGraphName),
                                    Uri.parse(nanopublicationHeadGraphName),
                                    Uri.parse(nanopublicationUri),
                                    getNanopublicationPart(nanopublicationProvenanceGraphName),
                                    getNanopublicationPart(nanopublicationPublicationInfoGraphName)
                            );
                        } catch (final MalformedNanopublicationException e) {
                            throw new MalformedNanopublicationRuntimeException(e);
                        }
                    }
                };
            }
        };
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
        return queryNanopublications(agraphQuery);
    }

    private QueryExecution queryNanopublications(final AGQuery query) {
        return AGQueryExecutionFactory.create(query, new AGModel(graphMaker.createGraph()));
    }
}
