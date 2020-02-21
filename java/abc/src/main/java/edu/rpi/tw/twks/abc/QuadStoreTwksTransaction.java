package edu.rpi.tw.twks.abc;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.nanopub.AutoCloseableIterable;
import edu.rpi.tw.twks.nanopub.DuplicateNanopublicationPartName;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationPart;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class QuadStoreTwksTransaction<TwksT extends AbstractTwks<?>> extends AbstractTwksTransaction {
    private final static Uri ASSERTIONS_GRAPH_NAME = Uri.parse("urn:twks:assertions");
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
    private final static Logger logger = LoggerFactory.getLogger(QuadStoreTwksTransaction.class);
    private final QuadStore quadStore;

    protected QuadStoreTwksTransaction(final QuadStore quadStore, final TwksT twks) {
        super(twks);
        this.quadStore = checkNotNull(quadStore);
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
        quadStore.deleteNamedGraphs(nanopublicationGraphNames);
        return DeleteNanopublicationResult.DELETED;
    }

    @Override
    public final void deleteNanopublications() {
        quadStore.deleteAllGraphs();
    }

    @Override
    public final Model getAssertions() {
        return quadStore.getNamedGraph(ASSERTIONS_GRAPH_NAME);
    }

//    private final Model getNamedGraphs(final Set<Uri> graphNames) {
//        final Model assertions = ModelFactory.createDefaultModel();
//        if (graphNames.isEmpty()) {
//            return assertions;
//        }
//        setNsPrefixes(assertions);
//        getNamedGraphs(graphNames, assertions);
//        return assertions;
//    }

    private ImmutableSet<Uri> getNanopublicationGraphNames(final Uri nanopublicationUri) {
        final ImmutableSet.Builder<Uri> resultBuilder = ImmutableSet.builder();
        try (final QueryExecution queryExecution = quadStore.query(QueryFactory.create(String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, nanopublicationUri)))) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                resultBuilder.add(Uri.parse(g.getURI()));
            }
        }
        return resultBuilder.build();
    }

    private ImmutableSet<Uri> getOntologyAssertionGraphNames(final ImmutableSet<Uri> ontologyUris) {
        return ontologyUris.stream().map(ontologyUri -> getOntologyAssertionsGraphName(ontologyUri)).collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        return quadStore.getNamedGraphs(getOntologyAssertionGraphNames(ontologyUris));
    }

    private Uri getOntologyAssertionsGraphName(final Uri ontologyUri) {
        try {
            return Uri.parse("urn:twks:assertions:ontology:" + URLEncoder.encode(ontologyUri.toString(), Charsets.UTF_8.name()));
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected abstract AutoCloseableIterable<Nanopublication> iterateNanopublications();

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
            if (quadStore.headNamedGraph(nanopublicationPart.getName())) {
                throw new DuplicateNanopublicationPartName(nanopublicationPart.getName().toString());
            }
            quadStore.addNamedGraph(nanopublicationPart.getName(), nanopublicationPart.getModel());
        }

        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public final QueryExecution queryAssertions(final Query query) {
        query.addNamedGraphURI(ASSERTIONS_GRAPH_NAME.toString());
        return quadStore.query(query);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return quadStore.query(query);
    }
}
