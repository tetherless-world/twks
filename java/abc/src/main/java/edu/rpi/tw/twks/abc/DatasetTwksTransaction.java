package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction implements TwksTransaction {
    private final static String GET_ASSERTION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A where {\n" +
            "  graph ?H { ?NP np:hasAssertion ?A }\n" +
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

    private final TwksConfiguration configuration;
    private final DatasetTransaction datasetTransaction;
    private final Dataset dataset;

    protected DatasetTwksTransaction(final TwksConfiguration configuration, final Dataset dataset, final ReadWrite readWrite) {
        this.configuration = checkNotNull(configuration);
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

    @Override
    public final ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        return uris.stream().map(uri -> deleteNanopublication(uri)).collect(ImmutableList.toImmutableList());
    }

    @Override
    public final void dump() throws IOException {
        final Path dumpDirectoryPath = configuration.getDumpDirectoryPath();
        if (!Files.isDirectory(dumpDirectoryPath)) {
            logger.info("dump directory {} does not exist, creating", dumpDirectoryPath);
            Files.createDirectory(dumpDirectoryPath);
            logger.info("created dump directory {}", dumpDirectoryPath);
        }

        final Map<String, Uri> nanopublicationFileNames = new HashMap<>();
        try {
            try (final NanopublicationFactory.DatasetNanopublications nanopublications = NanopublicationFactory.DEFAULT.iterateNanopublicationsFromDataset(getDataset(), getDatasetTransaction())) {
                for (final Nanopublication nanopublication : nanopublications) {
                    final String nanopublicationFileName = MoreFilenameUtils.cleanFileName(nanopublication.getUri().toString()) + ".trig";

                    {
                        @Nullable final Uri conflictNanopublicationUri = nanopublicationFileNames.get(nanopublicationFileName);
                        if (conflictNanopublicationUri != null) {
                            throw new IllegalStateException(String.format("duplicate nanopublication file name: %s (from URIs %s and %s)", nanopublicationFileName, nanopublication.getUri(), conflictNanopublicationUri));
                        }
                    }

                    nanopublicationFileNames.put(nanopublicationFileName, nanopublication.getUri());

                    final Path dumpFilePath = dumpDirectoryPath.resolve(nanopublicationFileName);
                    try (final FileOutputStream fileOutputStream = new FileOutputStream(dumpFilePath.toFile())) {
                        nanopublication.write(fileOutputStream);
                        logger.debug("wrote {} to {}", nanopublication.getUri(), dumpFilePath);
                    }
                }
            }
        } catch (final MalformedNanopublicationRuntimeException e) {
            logger.error("malformed nanopublication: ", e);
        }
    }

    private Set<String> getAssertionGraphNames() {
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
            return Optional.of(NanopublicationFactory.DEFAULT.createNanopublicationFromDataset(nanopublicationDataset));
        } catch (final MalformedNanopublicationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Dataset getNanopublicationDataset(final Uri uri) {
        try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uri)))) {
            return MoreDatasetFactory.createDatasetFromResultSet(queryExecution.execSelect());
        }
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

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return QueryExecutionFactory.create(query, getDataset());
    }
}
