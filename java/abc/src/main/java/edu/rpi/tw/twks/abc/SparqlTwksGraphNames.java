package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class SparqlTwksGraphNames implements TwksGraphNames {
    private final static String GET_ALL_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A where {\n" +
            "  graph ?H { ?NP np:hasAssertion ?A }\n" +
            "  graph ?A {?S ?P ?O}\n" +
            "}";
    private final static Query GET_ALL_ASSERTION_GRAPH_NAMES_QUERY = QueryFactory.create(GET_ALL_ASSERTION_GRAPH_NAMES_QUERY_STRING);
    private final static String GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";
    private final static String GET_ONTOLOGY_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix sio: <http://semanticscience.org/resource/>\n" +
            "select ?A where {\n" +
            "  graph ?H { ?NP a np:Nanopublication . ?NP np:hasAssertion ?A . ?NP np:hasPublicationInfo ?I . }\n" +
            "  graph ?I { ?NP sio:isAbout <%s> }\n" +
            "  graph ?A {?S ?P ?O}\n" +
            "}";

    private final Twks twks;

    public SparqlTwksGraphNames(final Twks twks) {
        this.twks = checkNotNull(twks);
    }

    @Override
    public final ImmutableSet<Uri> getAllAssertionGraphNames(final TwksTransaction transaction) {
        checkState(transaction.getTwks() == twks);

        final ImmutableSet.Builder<Uri> resultBuilder = ImmutableSet.builder();
        try (final QueryExecution queryExecution = transaction.queryNanopublications(GET_ALL_ASSERTION_GRAPH_NAMES_QUERY)) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("A");
                resultBuilder.add(Uri.parse(g.getURI()));
            }
        }
        return resultBuilder.build();
    }

    @Override
    public final ImmutableSet<Uri> getNanopublicationGraphNames(final Uri nanopublicationUri, final TwksTransaction transaction) {
        checkState(transaction.getTwks() == twks);

        final ImmutableSet.Builder<Uri> resultBuilder = ImmutableSet.builder();
        try (final QueryExecution queryExecution = transaction.queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, nanopublicationUri)))) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                resultBuilder.add(Uri.parse(g.getURI()));
            }
        }
        return resultBuilder.build();
    }

    @Override
    public final ImmutableSet<Uri> getOntologyAssertionGraphNames(final ImmutableSet<Uri> ontologyUris, final TwksTransaction transaction) {
        checkState(transaction.getTwks() == twks);

        final ImmutableSet.Builder<Uri> resultBuilder = ImmutableSet.builder();
        for (final Uri ontologyUri : ontologyUris) {
            try (final QueryExecution queryExecution = transaction.queryNanopublications(QueryFactory.create(String.format(GET_ONTOLOGY_ASSERTION_GRAPH_NAMES_QUERY_STRING, ontologyUri)))) {
                for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                    final QuerySolution querySolution = resultSet.nextSolution();
                    final Resource g = querySolution.getResource("A");
                    resultBuilder.add(Uri.parse(g.getURI()));
                }
            }
        }
        return resultBuilder.build();
    }

    @Override
    public final void invalidateCache() {
        // Nothing cached
    }
}
