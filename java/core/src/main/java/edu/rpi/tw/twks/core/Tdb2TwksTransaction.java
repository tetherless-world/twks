package edu.rpi.tw.twks.core;

import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationFactory;
import edu.rpi.tw.twks.nanopub.vocabulary.Vocabularies;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb2.TDB2;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final class Tdb2TwksTransaction extends DatasetTwksTransaction {
    private final static String GET_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A where {\n" +
            "  ?NP np:hasAssertion ?A\n" +
            "  graph ?A {?S ?P ?O}\n" +
            "}";

    // http://nanopub.org/guidelines/working_draft/
    private final static String GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";

    private final static String GET_NANOPUBLICATION_DATASET_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G ?S ?P ?O where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";

    Tdb2TwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final Set<String> nanopublicationGraphNames = getNanopublicationGraphNames(uri);
        if (nanopublicationGraphNames.isEmpty()) {
            return DeleteNanopublicationResult.NOT_FOUND;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        for (final String nanopublicationGraphName : nanopublicationGraphNames) {
            getDataset().removeNamedModel(nanopublicationGraphName);
        }
        return DeleteNanopublicationResult.DELETED;
    }

    private Set<String> getAssertionGraphNames() {
        final Set<String> assertionGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(GET_ASSERTION_GRAPH_NAMES_QUERY_STRING, getDataset())) {
            queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("A");
                assertionGraphNames.add(g.getURI());
            }
        }
        return assertionGraphNames;
    }

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        final Dataset nanopublicationDataset = getNanopublicationDataset(uri);
        if (!nanopublicationDataset.isEmpty()) {
            try {
                return Optional.of(NanopublicationFactory.getInstance().createNanopublicationFromDataset(nanopublicationDataset));
            } catch (final MalformedNanopublicationException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return Optional.empty();
        }
    }

    private Dataset getNanopublicationDataset(final Uri uri) {
        final String queryString = String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uri);
        final Query query = QueryFactory.create(queryString);
        final Dataset nanopublicationDataset = DatasetFactory.create();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, getDataset())) {
            queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                final RDFNode o = querySolution.get("O");
                final Property p = ResourceFactory.createProperty(querySolution.getResource("P").getURI());
                final Resource s = querySolution.getResource("S");

                Model model = nanopublicationDataset.getNamedModel(g.getURI());
                if (model == null) {
                    model = ModelFactory.createDefaultModel();
                    Vocabularies.setNsPrefixes(model);
                    nanopublicationDataset.addNamedModel(g.getURI(), model);
                }
                model.add(s, p, o);
            }
        }
        return nanopublicationDataset;
    }

    private Set<String> getNanopublicationGraphNames(final Uri uri) {
        final String queryString = String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, uri);
        final Query query = QueryFactory.create(queryString);
        final Set<String> nanopublicationGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, getDataset())) {
            queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                nanopublicationGraphNames.add(g.getURI());
            }
        }
        return nanopublicationGraphNames;
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri());
        nanopublication.toDataset(getDataset(), getDatasetTransaction());
        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public QueryExecution queryAssertions(final Query query) {
        // https://jena.apache.org/documentation/tdb/dynamic_datasets.html
        // Using one or more FROM clauses, causes the default graph of the dataset to be the union of those graphs.
        final Set<String> assertionGraphNames = getAssertionGraphNames();
        for (final String assertionGraphName : assertionGraphNames) {
            query.addGraphURI(assertionGraphName);
        }
        return QueryExecutionFactory.create(query, getDataset());
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, getDataset());
        queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
        return queryExecution;
    }

}
