package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationFactory;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction implements TwksTransaction {
    private final static String GET_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A where {\n" +
            "  ?NP np:hasAssertion ?A\n" +
            "  graph ?A {?S ?P ?O}\n" +
            "}";

    private final static Query GET_ASSERTION_GRAPH_NAMES_QUERY = QueryFactory.create(GET_ASSERTION_GRAPH_NAMES_QUERY_STRING);

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
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwksTransaction.class);
    private final DatasetTransaction datasetTransaction;
    private final Dataset dataset;

    protected DatasetTwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        this.dataset = checkNotNull(dataset);
        this.datasetTransaction = new DatasetTransaction(dataset, readWrite);
    }

    @Override
    public final void abort() {
        datasetTransaction.abort();
    }

    @Override
    public final void close() {
        datasetTransaction.close();
    }

    @Override
    public final void commit() {
        datasetTransaction.commit();
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

    protected abstract Set<String> getAssertionGraphNames();

    public final DatasetTransaction getDatasetTransaction() {
        return datasetTransaction;
    }

    @Override
    public final Model getAssertions() {
        final Set<String> assertionGraphNames = getAssertionGraphNames();
        final Model assertions = ModelFactory.createDefaultModel();
        if (assertionGraphNames.isEmpty()) {
            return assertions;
        }
        setNsPrefixes(assertions);
        for (final String assertionGraphName : assertionGraphNames) {
            final Model assertion = getDataset().getNamedModel(assertionGraphName);
            assertions.add(assertion);
        }
        return assertions;
    }

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        final Dataset nanopublicationDataset = getNanopublicationDataset(uri);
        if (nanopublicationDataset.isEmpty()) {
            return Optional.empty();
        }
        try {
            final ImmutableList<Nanopublication> nanopublications = NanopublicationFactory.getInstance().createNanopublicationsFromDataset(nanopublicationDataset);
            switch (nanopublications.size()) {
                case 0:
                    throw new IllegalStateException();
                case 1:
                    return Optional.of(nanopublications.get(0));
                default:
                    throw new IllegalStateException();
            }
        } catch (final MalformedNanopublicationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Dataset getNanopublicationDataset(final Uri uri) {
        final Dataset nanopublicationDataset = DatasetFactory.create();
        try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uri)))) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                final RDFNode o = querySolution.get("O");
                final Property p = ResourceFactory.createProperty(querySolution.getResource("P").getURI());
                final Resource s = querySolution.getResource("S");

                Model model = nanopublicationDataset.getNamedModel(g.getURI());
                if (model == null) {
                    model = ModelFactory.createDefaultModel();
                    setNsPrefixes(model);
                    nanopublicationDataset.addNamedModel(g.getURI(), model);
                }
                model.add(s, p, o);
            }
        }
        return nanopublicationDataset;
    }

    private Set<String> getNanopublicationGraphNames(final Uri uri) {
        final Set<String> nanopublicationGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, uri)))) {
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
        if (assertionGraphNames.isEmpty()) {
            logger.warn("no assertion graph names, querying empty model");
            return QueryExecutionFactory.create(query, ModelFactory.createDefaultModel());
        }
        for (final String assertionGraphName : assertionGraphNames) {
            query.addGraphURI(assertionGraphName);
        }
        return QueryExecutionFactory.create(query, dataset);
    }

    protected final Dataset getDataset() {
        return dataset;
    }
}
