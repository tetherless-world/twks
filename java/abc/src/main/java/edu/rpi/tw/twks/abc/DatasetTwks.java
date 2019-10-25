package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationRuntimeException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationFactory;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class DatasetTwks extends AbstractTwks {
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwks.class);
    private final static int[] fileNameIllegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    static {
        Arrays.sort(fileNameIllegalChars);
    }

    private final Dataset dataset;

    protected DatasetTwks(final TwksConfiguration configuration, final Dataset dataset) {
        super(configuration);
        this.dataset = checkNotNull(dataset);
    }

    private static String cleanFileName(final String badFileName) {
        final StringBuilder cleanName = new StringBuilder();
        final int len = badFileName.codePointCount(0, badFileName.length());
        for (int i = 0; i < len; i++) {
            final int c = badFileName.codePointAt(i);
            if (Arrays.binarySearch(fileNameIllegalChars, c) < 0) {
                cleanName.appendCodePoint(c);
            }
        }
        return cleanName.toString();
    }

    @Override
    public final void dump() throws IOException {
        final Path dumpDirectoryPath = getConfiguration().getDumpDirectoryPath();
        if (!Files.isDirectory(dumpDirectoryPath)) {
            logger.info("dump directory {} does not exist, creating", dumpDirectoryPath);
            Files.createDirectory(dumpDirectoryPath);
            logger.info("created dump directory {}", dumpDirectoryPath);
        }

        final Map<String, Uri> nanopublicationFileNames = new HashMap<>();
        try {
            try (final NanopublicationFactory.DatasetNanopublications nanopublications = NanopublicationFactory.DEFAULT.iterateNanopublicationsFromDataset(getDataset())) {
                for (final Nanopublication nanopublication : nanopublications) {
                    final String nanopublicationFileName = cleanFileName(nanopublication.getUri().toString()) + ".trig";

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

    protected final Dataset getDataset() {
        return dataset;
    }
}
