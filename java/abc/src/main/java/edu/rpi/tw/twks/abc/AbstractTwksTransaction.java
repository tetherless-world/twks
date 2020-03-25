package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTwksTransaction<TwksT extends AbstractTwks<?>> implements TwksTransaction {
    private final static String GET_NANOPUBLICATION_DATASET_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G ?S ?P ?O where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";
    private final static Query IS_EMPTY_QUERY = QueryFactory.create("SELECT (COUNT(?s) as ?count) WHERE { graph ?g { ?s ?p ?o } } LIMIT 1");

    private final static Logger logger = LoggerFactory.getLogger(AbstractTwksTransaction.class);
    private final TwksT twks;

    protected AbstractTwksTransaction(final TwksT twks) {
        this.twks = checkNotNull(twks);
    }

    @Override
    public ImmutableList<DeleteNanopublicationResult> deleteNanopublications(final ImmutableList<Uri> uris) {
        return uris.stream().map(uri -> deleteNanopublication(uri)).collect(ImmutableList.toImmutableList());
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

    protected final Dataset getNanopublicationDataset(final Uri uri) {
        try (final QueryExecution queryExecution = queryNanopublications(QueryFactory.create(String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uri)))) {
            return MoreDatasetFactory.createDatasetFromResultSet(queryExecution.execSelect());
        }
    }

    @Override
    public final TwksT getTwks() {
        return twks;
    }

    @Override
    public boolean isEmpty() {
        try (final QueryExecution queryExecution = queryNanopublications(IS_EMPTY_QUERY)) {
            final ResultSet resultSet = queryExecution.execSelect();
            if (!resultSet.hasNext()) {
                return true;
            }
            final QuerySolution solution = resultSet.next();
            final int count = solution.getLiteral("count").getInt();
            return count == 0;
        }
    }

    protected abstract AutoCloseableIterable<Nanopublication> iterateNanopublications();

    @Override
    public ImmutableList<PutNanopublicationResult> postNanopublications(final ImmutableList<Nanopublication> nanopublications) {
        return nanopublications.stream().map(nanopublication -> putNanopublication(nanopublication)).collect(ImmutableList.toImmutableList());
    }
}
