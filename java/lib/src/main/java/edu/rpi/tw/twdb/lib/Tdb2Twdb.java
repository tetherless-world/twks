package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.*;
import edu.rpi.tw.nanopub.vocabulary.Vocabularies;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.dmfs.rfc3986.Uri;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class Tdb2Twdb implements Twdb {
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

    private final Dataset tdbDataset;

    public Tdb2Twdb() {
        this.tdbDataset = TDB2Factory.createDataset();
    }

    @Override
    public final TwdbTransaction beginTransaction(final ReadWrite readWrite) {
        return new DatasetTwdbTransaction(tdbDataset, readWrite);
    }

    @Override
    public final boolean deleteNanopublication(final Uri uri) {
        try (final DatasetTransaction transaction = new DatasetTransaction(tdbDataset, ReadWrite.WRITE)) {
            final boolean result = deleteNanopublication(uri, transaction);
            if (result) {
                transaction.commit();
            } else {
                transaction.abort();
            }
            return result;
        }
    }

    @Override
    public final boolean deleteNanopublication(final Uri uri, final TwdbTransaction transaction) {
        return deleteNanopublication(uri, ((DatasetTwdbTransaction) transaction).getDatasetTransaction());
    }

    private final boolean deleteNanopublication(final Uri uri, final DatasetTransaction transaction) {
        final Set<String> nanopublicationGraphNames = getNanopublicationGraphNames(uri, transaction);
        if (nanopublicationGraphNames.isEmpty()) {
            return false;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        for (final String nanopublicationGraphName : nanopublicationGraphNames) {
            tdbDataset.removeNamedModel(nanopublicationGraphName);
        }
        return true;
    }

    private Set<String> getAssertionGraphNames(final DatasetTransaction transaction) {
        final Set<String> assertionGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(GET_ASSERTION_GRAPH_NAMES_QUERY_STRING, tdbDataset)) {
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
        try (final DatasetTransaction transaction = new DatasetTransaction(tdbDataset, ReadWrite.READ)) {
            return getNanopublication(uri, transaction);
        }
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final Uri uri, final TwdbTransaction transaction) {
        return getNanopublication(uri, ((DatasetTwdbTransaction) transaction).getDatasetTransaction());
    }

    private Optional<Nanopublication> getNanopublication(final Uri uri, final DatasetTransaction transaction) {
        final Dataset nanopublicationDataset = getNanopublicationDataset(uri, transaction);
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

    private Dataset getNanopublicationDataset(final Uri uri, final DatasetTransaction transaction) {
        final String uriString = Uris.toString(uri);
        final String queryString = String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uriString);
        final Query query = QueryFactory.create(queryString);
        final Dataset nanopublicationDataset = DatasetFactory.create();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset)) {
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

    private Set<String> getNanopublicationGraphNames(final Uri uri, final DatasetTransaction transaction) {
        final String uriString = Uris.toString(uri);
        final String queryString = String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, uriString);
        final Query query = QueryFactory.create(queryString);
        final Set<String> nanopublicationGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset)) {
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
    public final void putNanopublication(final Nanopublication nanopublication) {
        try (final DatasetTransaction transaction = new DatasetTransaction(tdbDataset, ReadWrite.WRITE)) {
            putNanopublication(nanopublication, transaction);
            transaction.commit();
        }
    }

    @Override
    public void putNanopublication(final Nanopublication nanopublication, final TwdbTransaction transaction) {
        putNanopublication(nanopublication, ((DatasetTwdbTransaction) transaction).getDatasetTransaction());
    }

    private final void putNanopublication(final Nanopublication nanopublication, final DatasetTransaction transaction) {
        deleteNanopublication(nanopublication.getUri(), transaction);
        nanopublication.toDataset(tdbDataset, transaction);
    }

    @Override
    public QueryExecution queryAssertions(final Query query, final TwdbTransaction transaction) {
        // https://jena.apache.org/documentation/tdb/dynamic_datasets.html
        // Using one or more FROM clauses, causes the default graph of the dataset to be the union of those graphs.
        final Set<String> assertionGraphNames = getAssertionGraphNames(((DatasetTwdbTransaction) transaction).getDatasetTransaction());
        for (final String assertionGraphName : assertionGraphNames) {
            query.addGraphURI(assertionGraphName);
        }
        return QueryExecutionFactory.create(query, tdbDataset);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query, final TwdbTransaction transaction) {
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
        return queryExecution;
    }
}
