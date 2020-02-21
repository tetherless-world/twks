package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

public abstract class AbstractTwksTransaction<TwksT extends AbstractTwks<?>> implements TwksTransaction {
    private final static String GET_NANOPUBLICATION_DATASET_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G ?S ?P ?O where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";

    private final static Logger logger = LoggerFactory.getLogger(AbstractTwksTransaction.class);
    private final TwksGraphNames graphNames;
    private final TwksT twks;

    protected AbstractTwksTransaction(final TwksGraphNames graphNames, final TwksT twks) {
        this.graphNames = checkNotNull(graphNames);
        this.twks = checkNotNull(twks);
    }

    protected abstract void addNamedGraph(final Uri name, final Model model);

    protected abstract void deleteAllGraphs();

    protected abstract void deleteNamedGraphs(final Set<Uri> graphNames);

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final DeleteNanopublicationResult result = deleteNanopublicationImpl(uri);
        // deleteNanopublicationImpl uses graphNames, so wait until after the operation to invalidate the graph names cache.
        graphNames.invalidateCache();
        return result;
    }

    private DeleteNanopublicationResult deleteNanopublicationImpl(final Uri uri) {
        final Set<Uri> nanopublicationGraphNames = graphNames.getNanopublicationGraphNames(uri, this);
        if (nanopublicationGraphNames.isEmpty()) {
            return DeleteNanopublicationResult.NOT_FOUND;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        deleteNamedGraphs(nanopublicationGraphNames);
        return DeleteNanopublicationResult.DELETED;
    }

    @Override
    public final ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        final ImmutableList<DeleteNanopublicationResult> results = uris.stream().map(uri -> deleteNanopublicationImpl(uri)).collect(ImmutableList.toImmutableList());
        // deleteNanopublicationImpl uses graphNames, so wait until after the operation to invalidate the graph names cache.
        graphNames.invalidateCache();
        return results;
    }

    @Override
    public final void deleteNanopublications() {
        deleteAllGraphs();
        // Wait until after the operation to invalidate the graph names cache.
        graphNames.invalidateCache();
    }

    @Override
    public final void dump() throws IOException {
        final Path dumpDirectoryPath = twks.getConfiguration().getDumpDirectoryPath();
        if (!Files.isDirectory(dumpDirectoryPath)) {
            logger.info("dump directory {} does not exist, creating", dumpDirectoryPath);
            Files.createDirectory(dumpDirectoryPath);
            logger.info("created dump directory {}", dumpDirectoryPath);
        }

        final Map<String, Uri> nanopublicationFileNames = new HashMap<>();
        try {
            try (final AutoCloseableIterable<Nanopublication> nanopublications = iterateNanopublications()) {
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

    @Override
    public final Model getAssertions() {
        return getNamedGraphs(graphNames.getAllAssertionGraphNames(this));
    }

    private final Model getNamedGraphs(final Set<Uri> graphNames) {
        final Model assertions = ModelFactory.createDefaultModel();
        if (graphNames.isEmpty()) {
            return assertions;
        }
        setNsPrefixes(assertions);
        getNamedGraphs(graphNames, assertions);
        return assertions;
    }

    protected abstract void getNamedGraphs(Set<Uri> graphNames, Model intoModel);

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) {
        final Dataset nanopublicationDataset = getNanopublicationDataset(uri);
        if (nanopublicationDataset.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(DatasetNanopublications.copyOne(nanopublicationDataset));
        } catch (final MalformedNanopublicationException e) {
            throw new IllegalStateException(e);
        }
    }

    private Dataset getNanopublicationDataset(final Uri uri) {
        try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uri)))) {
            return MoreDatasetFactory.createDatasetFromResultSet(queryExecution.execSelect());
        }
    }

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        return getNamedGraphs(graphNames.getOntologyAssertionGraphNames(ontologyUris, this));
    }

    @Override
    public final TwksT getTwks() {
        return twks;
    }

    protected abstract boolean headNamedGraph(Uri graphName);

    protected abstract AutoCloseableIterable<Nanopublication> iterateNanopublications();

    @Override
    public final ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        final ImmutableList<PutNanopublicationResult> results = nanopublications.stream().map(nanopublication -> putNanopublicationImpl(nanopublication)).collect(ImmutableList.toImmutableList());
        // putNanopublicationImpl may use graphNames, so wait until after the operation to invalidate the graph names cache.
        graphNames.invalidateCache();
        return results;
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
            if (headNamedGraph(nanopublicationPart.getName())) {
                throw new DuplicateNanopublicationPartName(name);
            }
            addNamedGraph(nanopublicationPart.getName(), nanopublicationPart.getModel());
        }

        final PutNanopublicationResult result = deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;

        // Subclass may use graphNames, so wait until after the operation to invalidate the graph names cache.
        graphNames.invalidateCache();

        return result;
    }

    @Override
    public final QueryExecution queryAssertions(final Query query) {
        // https://jena.apache.org/documentation/tdb/dynamic_datasets.html
        // Using one or more FROM clauses, causes the default graph of the dataset to be the union of those graphs.
        final Set<Uri> assertionGraphNames = graphNames.getAllAssertionGraphNames(this);
        if (assertionGraphNames.isEmpty()) {
            logger.warn("no assertion graph names, querying empty model");
            return QueryExecutionFactory.create(query, ModelFactory.createDefaultModel());
        }
        for (final Uri assertionGraphName : assertionGraphNames) {
            query.addGraphURI(assertionGraphName.toString());
        }
        return queryNanopublications(query);
    }
}
