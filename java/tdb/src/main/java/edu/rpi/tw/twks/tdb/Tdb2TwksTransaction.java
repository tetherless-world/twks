package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.abc.DatasetTwksTransaction;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb2.TDB2;

import java.util.HashSet;
import java.util.Set;

final class Tdb2TwksTransaction extends DatasetTwksTransaction {
    private final static String GET_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A where {\n" +
            "  ?NP np:hasAssertion ?A\n" +
            "  graph ?A {?S ?P ?O}\n" +
            "}";

    private final static Query GET_ASSERTION_GRAPH_NAMES_QUERY = QueryFactory.create(GET_ASSERTION_GRAPH_NAMES_QUERY_STRING);

    Tdb2TwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }

    @Override
    protected final Set<String> getAssertionGraphNames() {
        final Set<String> assertionGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = queryNanopublications(GET_ASSERTION_GRAPH_NAMES_QUERY)) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("A");
                assertionGraphNames.add(g.getURI());
            }
        }
        return assertionGraphNames;
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, getDataset());
        queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
        return queryExecution;
    }
}
