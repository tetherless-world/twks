package edu.rpi.tw.twks.abc;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.Vocabularies;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class QuadStoreTwksTransaction<TwksT extends AbstractTwks<?>> extends AbstractTwksTransaction<TwksT> {
    private final static Uri ASSERTIONS_UNION_GRAPH_NAME = Uri.parse("urn:twks:assertions");
    private final static String GET_NANOPUBLICATION_DATASET_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G ?S ?P ?O where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";
    private final static String GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";
    private final static String ITERATE_NANOPUBLICATIONS_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A ?H ?I ?np ?P where {\n" +
            "graph ?H {\n" +
            "  ?np a np:Nanopublication .\n" +
            "  ?np np:hasAssertion ?A .\n" +
            "  ?np np:hasProvenance ?P .\n" +
            "  ?np np:hasPublicationInfo ?I .\n" +
            "}}";
    private final static Query ITERATE_NANOPUBLICATIONS_QUERY = QueryFactory.create(ITERATE_NANOPUBLICATIONS_QUERY_STRING);
    private final static Logger logger = LoggerFactory.getLogger(QuadStoreTwksTransaction.class);
    private final QuadStoreTransaction quadStoreTransaction;

    protected QuadStoreTwksTransaction(final QuadStoreTransaction quadStore, final TwksT twks) {
        super(twks);
        this.quadStoreTransaction = checkNotNull(quadStore);
    }

    @Override
    public final void abort() {
        quadStoreTransaction.abort();
    }

    private Uri buildOntologyAssertionsGraphName(final Uri ontologyUri) {
        try {
            return Uri.parse("urn:twks:assertions:ontology:" + URLEncoder.encode(ontologyUri.toString(), Charsets.UTF_8.name()));
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final void close() {
        quadStoreTransaction.close();
    }

    @Override
    public final void commit() {
        quadStoreTransaction.commit();
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final Set<Uri> nanopublicationGraphNames = getNanopublicationGraphNames(uri);
        if (nanopublicationGraphNames.isEmpty()) {
            return DeleteNanopublicationResult.NOT_FOUND;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        for (final Uri nanopublicationGraphName : nanopublicationGraphNames) {
            quadStoreTransaction.removeNamedGraph(nanopublicationGraphName);
        }
        return DeleteNanopublicationResult.DELETED;
    }

    @Override
    public final void deleteNanopublications() {
        quadStoreTransaction.removeAllGraphs();
    }

    @Override
    public final Model getAssertions() {
        return quadStoreTransaction.getNamedGraph(ASSERTIONS_UNION_GRAPH_NAME);
    }

    private ImmutableSet<Uri> getNanopublicationGraphNames(final Uri nanopublicationUri) {
        final ImmutableSet.Builder<Uri> resultBuilder = ImmutableSet.builder();
        try (final QueryExecution queryExecution = quadStoreTransaction.query(QueryFactory.create(String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, nanopublicationUri)))) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                resultBuilder.add(Uri.parse(g.getURI()));
            }
        }
        return resultBuilder.build();
    }

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        final Model result = ModelFactory.createDefaultModel();
        if (ontologyUris.isEmpty()) {
            return result;
        }
        Vocabularies.setNsPrefixes(result);
        for (final Uri ontologyUri : ontologyUris) {
            result.add(quadStoreTransaction.getNamedGraph(buildOntologyAssertionsGraphName(ontologyUri)));
        }
        return result;
    }

    protected final QuadStoreTransaction getQuadStoreTransaction() {
        return quadStoreTransaction;
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
                        final Uri nanopublicationPartNameUri = Uri.parse(nanopublicationPartName);
                        final Model nanopublicationPartModel = quadStoreTransaction.getNamedGraph(nanopublicationPartNameUri);
                        return new NanopublicationPart(nanopublicationPartModel, nanopublicationPartNameUri);
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
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri());
        switch (deleteResult) {
            case DELETED:
                logger.debug("deleted nanopublication {} before put", nanopublication.getUri());
                break;
            case NOT_FOUND:
                logger.debug("nanopublication {} did not exist before put", nanopublication.getUri());
                break;
        }

        for (final NanopublicationPart nanopublicationPart : new NanopublicationPart[]{nanopublication.getAssertion(), nanopublication.getHead(), nanopublication.getProvenance(), nanopublication.getPublicationInfo()}) {
            if (quadStoreTransaction.containsNamedGraph(nanopublicationPart.getName())) {
                throw new DuplicateNanopublicationPartName(nanopublicationPart.getName().toString());
            }
            quadStoreTransaction.addNamedGraph(nanopublicationPart.getName(), nanopublicationPart.getModel());
        }

        final Model assertionsUnionGraph = quadStoreTransaction.getOrCreateNamedGraph(ASSERTIONS_UNION_GRAPH_NAME);
        assertionsUnionGraph.add(nanopublication.getAssertion().getModel());

        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public final QueryExecution queryAssertions(final Query query) {
        query.addGraphURI(ASSERTIONS_UNION_GRAPH_NAME.toString());
        return quadStoreTransaction.query(query);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return quadStoreTransaction.query(query);
    }
}
