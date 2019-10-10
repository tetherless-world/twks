package edu.rpi.tw.twks.core;

import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationFactory;
import edu.rpi.tw.twks.nanopub.vocabulary.Vocabularies;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

final class Tdb2Twks implements Twks {
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

    public Tdb2Twks() {
        this(Location.mem());
    }

    public Tdb2Twks(final Location location) {
        this.tdbDataset = TDB2Factory.connectDataset(location);
    }

    @Override
    public final TwksTransaction beginTransaction(final ReadWrite readWrite) {
        return new DatasetTwksTransaction(tdbDataset, readWrite);
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        try (final DatasetTransaction transaction = new DatasetTransaction(tdbDataset, ReadWrite.WRITE)) {
            final DeleteNanopublicationResult result = deleteNanopublication(uri, transaction);
            if (result == DeleteNanopublicationResult.DELETED) {
                transaction.commit();
            } else {
                transaction.abort();
            }
            return result;
        }
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri, final TwksTransaction transaction) {
        return deleteNanopublication(uri, ((DatasetTwksTransaction) transaction).getDatasetTransaction());
    }

    private final DeleteNanopublicationResult deleteNanopublication(final Uri uri, final DatasetTransaction transaction) {
        final Set<String> nanopublicationGraphNames = getNanopublicationGraphNames(uri, transaction);
        if (nanopublicationGraphNames.isEmpty()) {
            return DeleteNanopublicationResult.NOT_FOUND;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        for (final String nanopublicationGraphName : nanopublicationGraphNames) {
            tdbDataset.removeNamedModel(nanopublicationGraphName);
        }
        return DeleteNanopublicationResult.DELETED;
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
    public Optional<Nanopublication> getNanopublication(final Uri uri, final TwksTransaction transaction) {
        return getNanopublication(uri, ((DatasetTwksTransaction) transaction).getDatasetTransaction());
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
        final String queryString = String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uri);
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
        final String queryString = String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, uri);
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
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        try (final DatasetTransaction transaction = new DatasetTransaction(tdbDataset, ReadWrite.WRITE)) {
            final PutNanopublicationResult result = putNanopublication(nanopublication, transaction);
            transaction.commit();
            return result;
        }
    }

    @Override
    public PutNanopublicationResult putNanopublication(final Nanopublication nanopublication, final TwksTransaction transaction) {
        return putNanopublication(nanopublication, ((DatasetTwksTransaction) transaction).getDatasetTransaction());
    }

    private final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication, final DatasetTransaction transaction) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri(), transaction);
        nanopublication.toDataset(tdbDataset, transaction);
        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public QueryExecution queryAssertions(final Query query, final TwksTransaction transaction) {
        // https://jena.apache.org/documentation/tdb/dynamic_datasets.html
        // Using one or more FROM clauses, causes the default graph of the dataset to be the union of those graphs.
        final Set<String> assertionGraphNames = getAssertionGraphNames(((DatasetTwksTransaction) transaction).getDatasetTransaction());
        for (final String assertionGraphName : assertionGraphNames) {
            query.addGraphURI(assertionGraphName);
        }
        return QueryExecutionFactory.create(query, tdbDataset);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query, final TwksTransaction transaction) {
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset);
        queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
        return queryExecution;
    }
}
