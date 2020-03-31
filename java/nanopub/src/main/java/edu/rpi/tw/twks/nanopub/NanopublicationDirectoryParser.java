package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableMultimap;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Parser for a file system directory of nanopublications.
 * <p>
 * Wraps a NanopublicationParser because the signatures are very different than the former's parseX methods.
 */
public final class NanopublicationDirectoryParser {
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationDirectoryParser.class);
    private final NanopublicationParser nanopublicationParser;

    public NanopublicationDirectoryParser(final NanopublicationParser nanopublicationParser) {
        this.nanopublicationParser = checkNotNull(nanopublicationParser);
    }

    public final ImmutableMultimap<Path, Nanopublication> parseDirectory(final File sourceDirectoryPath) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationDirectoryConsumer consumer = new CollectingNanopublicationDirectoryConsumer();
        parseDirectory(sourceDirectoryPath, consumer);
        return consumer.build();
    }

    public final void parseDirectory(File sourceDirectoryPath, final NanopublicationDirectoryConsumer consumer) {
        if (nanopublicationParser.getDialect() == NanopublicationDialect.SPECIFICATION) {
            // Assume it's a directory where every .trig file is a nanopublication.
            final File[] sourceFiles = sourceDirectoryPath.listFiles();
            if (sourceFiles == null) {
                return;
            }
            for (final File trigFile : sourceFiles) {
                if (!trigFile.isFile()) {
                    continue;
                }
                if (!trigFile.getName().endsWith(".trig")) {
                    continue;
                }
                final Path trigFilePath = trigFile.toPath();
                nanopublicationParser.parseFile(trigFilePath, new FileNanopublicationConsumer(consumer, trigFilePath));
            }
        } else if (nanopublicationParser.getDialect() == NanopublicationDialect.WHYIS) {
            if (sourceDirectoryPath.getName().equals("data")) {
                sourceDirectoryPath = new File(sourceDirectoryPath, "nanopublications");
            }
            if (sourceDirectoryPath.getName().equals("nanopublications")) {
                // Trawl all of the subdirectories of /data/nanopublications
                final File[] nanopublicationSubdirectories = sourceDirectoryPath.listFiles();
                if (nanopublicationSubdirectories == null) {
                    return;
                }

                for (final File nanopublicationSubdirectory : nanopublicationSubdirectories) {
                    if (!nanopublicationSubdirectory.isDirectory()) {
                        continue;
                    }
                    final File twksFile = new File(nanopublicationSubdirectory, "file.twks.trig");
                    // #106
                    // We've previously parsed this Whyis nanopublication and written in back as a spec-compliant nanopublication.
                    // The conversion has to create new urn:uuid: graph URIs, which means that subsequent conversions won't
                    // produce the same spec-compliant nanopublication. We cache the converted nanopublication on disk so
                    // re-parsing it always produces the same result.

                    if (twksFile.isFile()) {
                        final Path twksFilePath = twksFile.toPath();
                        nanopublicationParser.parseFile(twksFilePath, new FileNanopublicationConsumer(consumer, twksFilePath));
                    } else {
                        final File whyisFile = new File(nanopublicationSubdirectory, "file");
                        final Path whyisFilePath = whyisFile.toPath();
                        // Collect the nanopublications so we can also write them out, independently of the consumer.
                        final List<Nanopublication> twksNanopublications = new ArrayList<>();
                        nanopublicationParser.parseFile(whyisFilePath, new FileNanopublicationConsumer(consumer, whyisFilePath) {
                            @Override
                            public void accept(final Nanopublication nanopublication) {
                                super.accept(nanopublication);
                                twksNanopublications.add(nanopublication);
                            }
                        });
                        // Write the twksFile spec-compliant nanopublications for use later, in the branch above.
                        {
                            final Dataset dataset = DatasetFactory.create();
                            for (final Nanopublication nanopublication : twksNanopublications) {
                                nanopublication.toDataset(dataset);
                            }
                            try (final OutputStream twksFileOutputStream = new FileOutputStream(twksFile)) {
                                RDFDataMgr.write(twksFileOutputStream, dataset, Lang.TRIG);
                            } catch (final IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            } else {
                // Assume the directory contains a single nanopublication
                final File file = new File(sourceDirectoryPath, "file");
                final Path filePath = file.toPath();
                nanopublicationParser.parseFile(filePath, new FileNanopublicationConsumer(consumer, filePath));
            }
        }
    }

    private final static class CollectingNanopublicationDirectoryConsumer implements NanopublicationDirectoryConsumer {
        private final ImmutableMultimap.Builder<Path, Nanopublication> nanopublicationsBuilder = ImmutableMultimap.builder();

        @Override
        public final void accept(final Nanopublication nanopublication, final Path nanopublicationFilePath) {
            nanopublicationsBuilder.put(nanopublicationFilePath, nanopublication);
        }

        public final ImmutableMultimap<Path, Nanopublication> build() {
//            if (logger.isDebugEnabled()) {
//                logger.debug("parsed {} nanopublications from {}", result.size(), sourceDirectoryPath);
//            }
            return nanopublicationsBuilder.build();
        }

        @Override
        public final void onMalformedNanopublicationException(final MalformedNanopublicationException exception, final Path nanopublicationFilePath) {
            throw new MalformedNanopublicationRuntimeException(exception);
        }
    }

    private static class FileNanopublicationConsumer implements NanopublicationConsumer {
        private final NanopublicationDirectoryConsumer directoryConsumer;
        private final Path nanopublicationFilePath;

        public FileNanopublicationConsumer(final NanopublicationDirectoryConsumer directoryConsumer, final Path nanopublicationFilePath) {
            this.directoryConsumer = checkNotNull(directoryConsumer);
            this.nanopublicationFilePath = checkNotNull(nanopublicationFilePath);
        }

        @Override
        public void accept(final Nanopublication nanopublication) {
            directoryConsumer.accept(nanopublication, nanopublicationFilePath);
        }

        @Override
        public void onMalformedNanopublicationException(final MalformedNanopublicationException exception) {
            directoryConsumer.onMalformedNanopublicationException(exception, nanopublicationFilePath);
        }
    }
}
