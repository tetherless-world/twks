package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.ByteStreams;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Nanopublication parser. Parse methods are of the form:
 * <p>
 * parseX(source, consumer) -> void
 * or
 * parseX(source) -> list of nanopublications
 * <p>
 * In the latter case parser exceptions (e.g., MalformedNanopublicationException) are thrown as runtime exceptions.
 */
public class NanopublicationParser {
    public final static NanopublicationParser DEFAULT = new NanopublicationParser(NanopublicationDialect.SPECIFICATION, Optional.of(NanopublicationDialect.SPECIFICATION.getDefaultLang()));
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationParser.class);
    private final NanopublicationDialect dialect;
    private final Optional<Lang> lang;

    public NanopublicationParser(final NanopublicationDialect dialect, final Optional<Lang> lang) {
        this.dialect = checkNotNull(dialect);
        this.lang = checkNotNull(lang);
    }

    public final static NanopublicationParserBuilder builder() {
        return new NanopublicationParserBuilder();
    }

    public final NanopublicationDialect getDialect() {
        return dialect;
    }

//    private ImmutableList<Uri> getNanopublicationUris(final ImmutableList<Nanopublication> nanopublications) {
//        return nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
//    }

    private RDFParserBuilder newRdfParserBuilder() {
        final RDFParserBuilder builder = RDFParserBuilder.create();
        builder.lang(lang.orElse(dialect.getDefaultLang()));
        return builder;
    }

    private void parse(final RDFParser rdfParser, final NanopublicationConsumer consumer, final Optional<Uri> sourceUri) {
        final Dataset dataset = DatasetFactory.create();

        try {
            rdfParser.parse(dataset);
        } catch (final RiotNotFoundException e) {
            throw e;
        } catch (final RiotException e) {
            consumer.onMalformedNanopublicationException(new MalformedNanopublicationException(e));
            return;
        }

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        final boolean datasetHasNamedGraphs;
        try {
            datasetHasNamedGraphs = dataset.listNames().hasNext();
        } catch (final UnsupportedOperationException e) {
            // Jena throws this exception when a graph name is a blank node
            // The latter appears to be legal TriG.
            consumer.onMalformedNanopublicationException(new MalformedNanopublicationException("blank node graph names not supported"));
            return;
        }
        if (datasetHasNamedGraphs) {
            parseDataset(dataset, consumer);
            return;
        }

        final NanopublicationBuilder nanopublicationBuilder = Nanopublication.builder();
        nanopublicationBuilder.getAssertionBuilder().setModel(dataset.getDefaultModel());
        if (sourceUri.isPresent()) {
            nanopublicationBuilder.getProvenanceBuilder().wasDerivedFrom(sourceUri.get());
        }
        final Nanopublication nanopublication;
        try {
            nanopublication = nanopublicationBuilder.build();
        } catch (final MalformedNanopublicationException e) {
            consumer.onMalformedNanopublicationException(e);
            return;
        }
        consumer.accept(nanopublication);
    }

    public final ImmutableList<Nanopublication> parse(final String source) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parse(source, consumer);
        return consumer.build();
    }

    public final void parse(final String source, final NanopublicationConsumer consumer) {
        if (source.equals("-")) {
            parseStdin(consumer);
            return;
        }

        final File sourceFile = new File(source);
        if (sourceFile.isFile()) {
            parseFile(sourceFile.toPath(), consumer);
            return;
        } else if (sourceFile.isDirectory()) {
            parseDirectory(sourceFile, new NanopublicationDirectoryConsumer() {
                @Override
                public void accept(final Nanopublication nanopublication, final Path nanopublicationFilePath) {
                    consumer.accept(nanopublication);
                }

                @Override
                public void onMalformedNanopublicationException(final MalformedNanopublicationException exception, final Path nanopublicationFilePath) {
                    consumer.onMalformedNanopublicationException(exception);
                }
            });
            return;
        }

        parseUrl(Uri.parse(source));
    }

    private void parseDataset(final Dataset dataset, final NanopublicationConsumer consumer) {
        try (final DatasetNanopublications datasetNanopublications = new DatasetNanopublications(dataset, dialect)) {
            final Iterator<Nanopublication> nanopublicationI = datasetNanopublications.iterator();
            while (nanopublicationI.hasNext()) {
                final Nanopublication nanopublication;
                try {
                    nanopublication = nanopublicationI.next();
                } catch (final MalformedNanopublicationRuntimeException e) {
                    consumer.onMalformedNanopublicationException(e.getCause());
                    continue;
                }
                consumer.accept(nanopublication);
            }
        }
    }

    public final ImmutableMultimap<Path, Nanopublication> parseDirectory(final File sourceDirectoryPath) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationDirectoryConsumer consumer = new CollectingNanopublicationDirectoryConsumer();
        parseDirectory(sourceDirectoryPath, consumer);
        return consumer.build();
    }

    public final void parseDirectory(File sourceDirectoryPath, final NanopublicationDirectoryConsumer consumer) {
        if (getDialect() == NanopublicationDialect.SPECIFICATION) {
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
                parseFile(trigFilePath, new FileNanopublicationConsumer(consumer, trigFilePath));
            }
        } else if (getDialect() == NanopublicationDialect.WHYIS) {
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
                        parseFile(twksFilePath, new FileNanopublicationConsumer(consumer, twksFilePath));
                    } else {
                        final File whyisFile = new File(nanopublicationSubdirectory, "file");
                        final Path whyisFilePath = whyisFile.toPath();
                        // Collect the nanopublications so we can also write them out, independently of the consumer.
                        final List<Nanopublication> twksNanopublications = new ArrayList<>();
                        parseFile(whyisFilePath, new FileNanopublicationConsumer(consumer, whyisFilePath) {
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
                parseFile(filePath, new FileNanopublicationConsumer(consumer, filePath));
            }
        }
    }

    public final ImmutableList<Nanopublication> parseFile(final Path filePath) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parseFile(filePath, consumer);
        return consumer.build();
    }

    public void parseFile(final Path filePath, final NanopublicationConsumer consumer) {
        parse(newRdfParserBuilder().source(filePath).build(), consumer, Optional.of(Uri.parse(checkNotNull(filePath).toUri().toString())));
    }

    public final ImmutableList<Nanopublication> parseStdin() throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parseStdin(consumer);
        return consumer.build();
    }

    public final void parseStdin(final NanopublicationConsumer consumer) {
        final byte[] trigBytes;
        try {
            trigBytes = ByteStreams.toByteArray(System.in);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final String trigString = new String(trigBytes);
        parseString(trigString, consumer);
    }

    public final ImmutableList<Nanopublication> parseString(final String string) throws MalformedNanopublicationRuntimeException {
        return parseString(string, Optional.empty());
    }

    public final void parseString(final String string, final NanopublicationConsumer consumer) {
        parseString(string, consumer, Optional.empty());
    }

    public final ImmutableList<Nanopublication> parseString(final String string, final Optional<Uri> sourceUri) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parseString(string, consumer, sourceUri);
        return consumer.build();
    }

    public final void parseString(final String string, final NanopublicationConsumer consumer, final Optional<Uri> sourceUri) {
        parse(newRdfParserBuilder().source(new StringReader(string)).build(), consumer, sourceUri);
    }

    public final ImmutableList<Nanopublication> parseUrl(final Uri url) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parseUrl(url, consumer);
        return consumer.build();
    }

    public final void parseUrl(final Uri url, final NanopublicationConsumer consumer) {
        parse(newRdfParserBuilder().source(url.toString()).build(), consumer, Optional.of(url));
    }

    private final static class CollectingNanopublicationConsumer implements NanopublicationConsumer {
        private final ImmutableList.Builder<Nanopublication> nanopublicationsBuilder = ImmutableList.builder();

        @Override
        public final void accept(final Nanopublication nanopublication) {
            nanopublicationsBuilder.add(nanopublication);
        }

        public final ImmutableList<Nanopublication> build() {
            return nanopublicationsBuilder.build();
        }

        @Override
        public final void onMalformedNanopublicationException(final MalformedNanopublicationException exception) throws MalformedNanopublicationRuntimeException {
            throw new MalformedNanopublicationRuntimeException(exception);
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
