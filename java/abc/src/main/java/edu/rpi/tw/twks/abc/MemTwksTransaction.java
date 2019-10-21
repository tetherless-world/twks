package edu.rpi.tw.twks.abc;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;

import java.util.HashSet;
import java.util.Set;

final class MemTwksTransaction extends DatasetTwksTransaction {
    private final static String GET_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A where {\n" +
            "  ?NP np:hasAssertion ?A\n" +
            "}";

    private final static Query GET_ASSERTION_GRAPH_NAMES_QUERY = QueryFactory.create(GET_ASSERTION_GRAPH_NAMES_QUERY_STRING);

    MemTwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }

    @Override
    protected final Set<String> getAssertionGraphNames() {
        final Set<String> assertionGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(GET_ASSERTION_GRAPH_NAMES_QUERY, getDataset().getUnionModel())) {
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
        return QueryExecutionFactory.create(query, getDataset());
    }
}
