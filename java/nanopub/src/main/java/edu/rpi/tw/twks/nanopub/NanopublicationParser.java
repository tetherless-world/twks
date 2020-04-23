package edu.rpi.tw.twks.nanopub;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.ByteStreams;
import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.NANOPUB;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.*;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationParser.class);
    private final static Predicate<Path> specificationNanopublicationDirectoryFilter = (Path path) -> {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        @Nullable final Lang sourceFileLang = RDFLanguages.filenameToLang(path.getFileName().toString());
        if (sourceFileLang == null) {
            logger.info("ignoring non-RDF file {}", path);
            return false;
        }
        return true;
    };
    private final int concurrencyLevel;
    private final NanopublicationDialect dialect;
    private final Optional<Lang> lang;
    private final Metrics metrics;

    public NanopublicationParser(final int concurrencyLevel, final NanopublicationDialect dialect, final Optional<Lang> lang, final MetricRegistry metricRegistry) {
        this.concurrencyLevel = concurrencyLevel;
        this.dialect = checkNotNull(dialect);
        this.lang = checkNotNull(lang);
        this.metrics = new Metrics(metricRegistry);
    }

    public final static NanopublicationParserBuilder builder() {
        return new NanopublicationParserBuilder();
    }

    public final NanopublicationDialect getDialect() {
        return dialect;
    }

    private RDFParserBuilder newRdfParserBuilder() {
        final RDFParserBuilder builder = RDFParserBuilder.create();
        if (lang.isPresent()) {
            builder.lang(lang.get());
        }
        return builder;
    }

//    private ImmutableList<Uri> getNanopublicationUris(final ImmutableList<Nanopublication> nanopublications) {
//        return nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
//    }

    private void parse(final RDFParser rdfParser, final NanopublicationConsumer consumer, final Optional<Uri> sourceUri) {
        final Dataset dataset = DatasetFactory.create();

        final Timer.Context rdfParserTimerContext = metrics.rdfParserTimer.time();
        try {
            rdfParser.parse(dataset);
            rdfParserTimerContext.stop(); // Only record the time for successful parses.
        } catch (final RiotNotFoundException e) {
            throw e;
        } catch (final RuntimeIOException e) {
            if (e.getCause() instanceof MalformedInputException) {
                consumer.onMalformedNanopublicationException(new MalformedNanopublicationException((MalformedInputException) e.getCause()));
            } else {
                throw e;
            }
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

        final Path sourcePath = Paths.get(source);

        if (Files.isRegularFile(sourcePath)) {
            parseFile(sourcePath, consumer);
            return;
        }

        if (Files.isDirectory(sourcePath)) {
            parseDirectory(sourcePath, new NanopublicationDirectoryConsumer() {
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

    public final void parseDataset(final Dataset dataset, final NanopublicationConsumer consumer) {
        try (final DatasetNanopublicationParser parser = new DatasetNanopublicationParser(true, new DatasetTransaction(dataset, ReadWrite.READ))) {
            parser.parse(consumer);
        }
    }

    public final void parseDataset(final DatasetTransaction datasetTransaction, final NanopublicationConsumer consumer) {
        try (final DatasetNanopublicationParser parser = new DatasetNanopublicationParser(false, datasetTransaction)) {
            parser.parse(consumer);
        }
    }

    public final ImmutableList<Nanopublication> parseDataset(final Dataset dataset) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parseDataset(dataset, consumer);
        return consumer.build();
    }

    public final ImmutableMultimap<Path, Nanopublication> parseDirectory(final Path sourceDirectoryPath) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationDirectoryConsumer consumer = new CollectingNanopublicationDirectoryConsumer();
        parseDirectory(sourceDirectoryPath, consumer);
        return consumer.build();
    }

//    public final ImmutableList<Nanopublication> parseDataset(final DatasetTransaction datasetTransaction) throws MalformedNanopublicationRuntimeException {
//        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
//        parseDataset(datasetTransaction, consumer);
//        return consumer.build();
//    }

    public final void parseDirectory(final Path sourceDirectoryPath, final NanopublicationDirectoryConsumer consumer) {
        if (getDialect() == NanopublicationDialect.SPECIFICATION) {
            parseSpecificationNanopublicationsDirectory(sourceDirectoryPath, consumer);
        } else if (getDialect() == NanopublicationDialect.WHYIS) {
            parseWhyisNanopublicationsDirectory(sourceDirectoryPath, consumer);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public final ImmutableList<Nanopublication> parseFile(final Path filePath) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parseFile(filePath, consumer);
        return consumer.build();
    }

    public void parseFile(final Path filePath, final NanopublicationConsumer consumer) {
        try (final Timer.Context timerContext = metrics.parseFileTimer.time()) {
            // If our lang was not specified, let Jena detect the lang from the file path instead of setting the default from the dialect.
            final String fileUri = filePath.toUri().toString();
            // Use source(file URI) instead of source (file Path object) to get around a bug in Jena, where it doesn't try to infer the
            // language from file paths, only from URIs.
            parse(newRdfParserBuilder().source(fileUri).build(), consumer, Optional.of(Uri.parse(fileUri)));
        }
    }

    public final ImmutableList<Nanopublication> parseInputStream(final InputStream inputStream) throws MalformedNanopublicationRuntimeException {
        return parseInputStream(inputStream, Optional.empty());
    }

    public final void parseInputStream(final InputStream inputStream, final NanopublicationConsumer consumer) {
        parseInputStream(inputStream, consumer, Optional.empty());
    }

    public final ImmutableList<Nanopublication> parseInputStream(final InputStream inputStream, final Optional<Uri> sourceUri) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationConsumer consumer = new CollectingNanopublicationConsumer();
        parseInputStream(inputStream, consumer, sourceUri);
        return consumer.build();
    }

    public final void parseInputStream(final InputStream inputStream, final NanopublicationConsumer consumer, final Optional<Uri> sourceUri) {
        parse(newRdfParserBuilder().source(inputStream).build(), consumer, sourceUri);
    }

    private void parseSpecificationNanopublicationsDirectory(final Path sourceDirectoryPath, final NanopublicationDirectoryConsumer consumer) {
        Stream<Path> sourceFilePathStream;
        try {
            sourceFilePathStream = Files.list(sourceDirectoryPath);
        } catch (final IOException e) {
            logger.info("error listing {}: ", sourceDirectoryPath, e);
            return;
        }

        sourceFilePathStream = sourceFilePathStream.filter(specificationNanopublicationDirectoryFilter);

        if (concurrencyLevel > 1) {
            final ExecutorService executorService = Executors.newFixedThreadPool(concurrencyLevel);
            sourceFilePathStream.forEach(sourceFilePath -> {
                executorService.submit(() -> {
                    parseFile(sourceFilePath, new FileNanopublicationConsumer(consumer, sourceFilePath));
                });
            });
        } else {
            sourceFilePathStream.forEach(sourceFilePath -> {
                parseFile(sourceFilePath, new FileNanopublicationConsumer(consumer, sourceFilePath));
            });
        }
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
        // If our lang was not specified, let Jena detect the lang from the URL instead of setting the default from the dialect.
        parse(newRdfParserBuilder().source(url.toString()).build(), consumer, Optional.of(url));
    }

    private void parseWhyisNanopublicationsDirectory(Path sourceDirectoryPath, final NanopublicationDirectoryConsumer consumer) {
        if (sourceDirectoryPath.getFileName().toString().equals("data")) {
            sourceDirectoryPath = sourceDirectoryPath.resolve("nanopublications");
        }
        if (!sourceDirectoryPath.getFileName().toString().equals("nanopublications")) {
            // Assume the directory contains a single nanopublication
            final Path filePath = sourceDirectoryPath.resolve("file");
            parseFile(filePath, new FileNanopublicationConsumer(consumer, filePath));
            return;
        }

        // Trawl all of the subdirectories of /data/nanopublications
        final ImmutableList<Path> nanopublicationSubdirectoryPaths;
        try {
            nanopublicationSubdirectoryPaths = Files.list(sourceDirectoryPath).collect(ImmutableList.toImmutableList());
        } catch (final IOException e) {
            logger.error("error listing {}: ", sourceDirectoryPath, e);
            return;
        }

        for (final Path nanopublicationSubdirectoryPath : nanopublicationSubdirectoryPaths) {
            if (!Files.isDirectory(nanopublicationSubdirectoryPath)) {
                continue;
            }

            final Path whyisFilePath = nanopublicationSubdirectoryPath.resolve("file");
            if (!Files.isRegularFile(whyisFilePath)) {
                // If there's no Whyis nanopublication file in the directory, the nanopublication may have been deleted. Ignore lingering TWKS files.
                logger.debug("no Whyis nanopublication file in {}, ignoring", nanopublicationSubdirectoryPath);
                continue;
            }

            final Path twksFilePath = nanopublicationSubdirectoryPath.resolve("file.twks.trig");
            if (Files.isRegularFile(twksFilePath)) {
                // #106
                // We've previously parsed this Whyis nanopublication and written in back as a spec-compliant nanopublication.
                // The conversion has to create new urn:uuid: graph URIs, which means that subsequent conversions won't
                // produce the same spec-compliant nanopublication. We cache the converted nanopublication on disk so
                // re-parsing it always produces the same result.
                // We wrote the file.twks.trig in specification TRIG, regardless of the lang of the current parser.
//                    checkState(NanopublicationParser.SPECIFICATION.getLang().equals(Lang.TRIG));
                parseFile(twksFilePath, new FileNanopublicationConsumer(consumer, twksFilePath));
                continue;
            }

            // Collect the nanopublications so we can also write them out, independently of the consumer.
            final List<Nanopublication> twksNanopublications = new ArrayList<>();
            // Track the nanopublication under the twksFilePath, since on subsequent passes it will be tracked that way in the branch above.
            // Force NQUADS
            parse(newRdfParserBuilder().lang(Lang.NQUADS).source(whyisFilePath).build(), new FileNanopublicationConsumer(consumer, twksFilePath) {
                @Override
                public void accept(final Nanopublication nanopublication) {
                    super.accept(nanopublication);
                    twksNanopublications.add(nanopublication);
                }
            }, Optional.of(Uri.parse(whyisFilePath.toUri().toString())));
            // Write the twksFile spec-compliant nanopublications for use later, in the branch above.
            {
                final Dataset dataset = DatasetFactory.create();
                for (final Nanopublication nanopublication : twksNanopublications) {
                    nanopublication.toDataset(dataset);
                }
                try (final OutputStream twksFileOutputStream = new FileOutputStream(twksFilePath.toFile())) {
                    // See note above re: file.twks.trig.
//                            checkState(NanopublicationParser.SPECIFICATION.getLang().equals(Lang.TRIG));
                    RDFDataMgr.write(twksFileOutputStream, dataset, Lang.TRIG);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
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

    private final static class Metrics {
        public final Timer getDatasetNanopublicationHeadsTimer;
        public final Timer parseDatasetNanopublicationTimer;
        public final Timer parseFileTimer;
        public final Timer rdfParserTimer;

        public Metrics(final MetricRegistry registry) {
            getDatasetNanopublicationHeadsTimer = registry.timer(MetricRegistry.name(NanopublicationParser.class, "getDatasetNanopublicationHeadsTimer"));
            parseDatasetNanopublicationTimer = registry.timer(MetricRegistry.name(NanopublicationParser.class, "parseDatasetNanopublicationTimer"));
            parseFileTimer = registry.timer(MetricRegistry.name(NanopublicationParser.class, "parseFileTimer"));
            rdfParserTimer = registry.timer(MetricRegistry.name(NanopublicationParser.class, "rdfParserTimer"));
        }
    }

    private final class DatasetNanopublicationParser implements AutoCloseable {
        private final Dataset dataset;
        private final boolean ownTransaction;
        private final DatasetTransaction transaction;

        public DatasetNanopublicationParser(final boolean ownTransaction, final DatasetTransaction transaction) {
            this.dataset = transaction.getDataset();
            this.ownTransaction = ownTransaction;
            this.transaction = checkNotNull(transaction);
        }

        @Override
        public final void close() {
            if (ownTransaction) {
                transaction.close();
            }
        }

        /**
         * Get the head named graphs in the Dataset.
         *
         * @return a map of nanopublication URI -> head
         * @throws MalformedNanopublicationException
         */
        private Map<Uri, NanopublicationPart> getHeads(final Set<String> unusedDatasetModelNames) throws MalformedNanopublicationException {
            try (final Timer.Context timerContext = metrics.getDatasetNanopublicationHeadsTimer.time()) {
                final Map<Uri, NanopublicationPart> headsByNanopublicationUri = new HashMap<>();
                for (final Iterator<String> unusedDatasetModelNameI = unusedDatasetModelNames.iterator(); unusedDatasetModelNameI.hasNext(); ) {
                    final String modelName = unusedDatasetModelNameI.next();
                    final Model model = dataset.getNamedModel(modelName);
                    final List<Resource> nanopublicationResources = model.listSubjectsWithProperty(RDF.type, NANOPUB.Nanopublication).toList();
                    switch (nanopublicationResources.size()) {
                        case 0:
                            continue;
                        case 1:
                            final Resource nanopublicationResource = nanopublicationResources.get(0);
                            if (nanopublicationResource.getURI() == null) {
                                throw new MalformedNanopublicationException("nanopublication resource is a blank node");
                            }
                            final Uri nanopublicationUri = Uri.parse(nanopublicationResource.getURI());
                            if (headsByNanopublicationUri.containsKey(nanopublicationUri)) {
                                throw new MalformedNanopublicationException(String.format("duplicate nanopublication URI %s", nanopublicationUri));
                            }
                            headsByNanopublicationUri.put(nanopublicationUri, new NanopublicationPart(model, Uri.parse(modelName)));
                            unusedDatasetModelNameI.remove();
                            break;
                        default:
                            // Specification: There is exactly one quad of the form '[N] rdf:type np:Nanopublication [H]', which identifies [N] as the nanopublication URI, and [H] as the head URI
                            throw new MalformedNanopublicationException(String.format("nanopublication head graph %s has more than one rdf:type Nanopublication", modelName));
                    }
                }
                return headsByNanopublicationUri;
            }
        }

        /**
         * Get the named model in a dataset that correspond to part of a nanopublication e.g., the named assertion graph.
         * The dataset contains
         * <nanopublication URI> nanopub:hasAssertion <assertion graph URI> .
         * The same goes for nanopub:hasProvenance and nanopub:hasPublicationInfo.
         */
        private NanopublicationPart getNanopublicationPart(final NanopublicationPart head, final Uri nanopublicationUri, final Property partProperty, final Set<String> unusedDatasetModelNames) throws MalformedNanopublicationException {
            final List<RDFNode> partRdfNodes = head.getModel().listObjectsOfProperty(ResourceFactory.createResource(nanopublicationUri.toString()), partProperty).toList();

            switch (partRdfNodes.size()) {
                case 0:
                    throw new MalformedNanopublicationException(String.format("nanopublication %s has no %s", nanopublicationUri, partProperty));
                case 1:
                    break;
                default:
                    throw new MalformedNanopublicationException(String.format("nanopublication %s has more than one %s", nanopublicationUri, partProperty));
            }

            final RDFNode partRdfNode = partRdfNodes.get(0);

            if (!(partRdfNode instanceof Resource)) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s is not a resource", nanopublicationUri, partProperty));
            }

            final Resource partResource = (Resource) partRdfNode;

            if (partResource.getURI() == null) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s is a blank node", nanopublicationUri, partProperty));
            }

            final String partModelName = partResource.toString();

            final Model partModel = dataset.getNamedModel(partModelName);
            if (partModel == null) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to a missing named graph (%s)", nanopublicationUri, partProperty, partResource));
            }

            if (partModel.isEmpty() && !dialect.allowEmptyPart()) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to an empty named graph (%s)", nanopublicationUri, partProperty, partResource));
            }

            if (!unusedDatasetModelNames.remove(partModelName) && !dialect.allowPartUriReuse()) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to a named graph that has already been used by another nanopublication", nanopublicationUri, partProperty, partResource));
            }

            final Uri partModelUri = Uri.parse(partModelName);

            return new NanopublicationPart(partModel, partModelUri);
        }

        public final void parse(final NanopublicationConsumer consumer) {
            final Set<String> unusedDatasetModelNames = new HashSet<>();
            dataset.listNames().forEachRemaining(modelName -> {
                if (unusedDatasetModelNames.contains(modelName)) {
                    throw new IllegalStateException();
                }
                unusedDatasetModelNames.add(modelName);
            });

            final Iterator<Map.Entry<Uri, NanopublicationPart>> headEntryI;

            // Specification: All triples must be placed in one of [H] or [A] or [P] or [I]
            try {
                if (!dialect.allowDefaultModelStatements() && !dataset.getDefaultModel().isEmpty()) {
                    throw new MalformedNanopublicationException("dataset contains statements in the default model");
                }

                headEntryI = getHeads(unusedDatasetModelNames).entrySet().iterator();
            } catch (final MalformedNanopublicationException e) {
                throw new MalformedNanopublicationRuntimeException(e);
            }

            while (headEntryI.hasNext()) {
                final Nanopublication nanopublication;
                try (final Timer.Context timerContext = metrics.parseDatasetNanopublicationTimer.time()) {
                    final Map.Entry<Uri, NanopublicationPart> headEntry = headEntryI.next();
                    final Uri nanopublicationUri = headEntry.getKey();
                    final NanopublicationPart head = headEntry.getValue();

                    try {
                        // Specification: Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasAssertion [A] [H]', which identifies [A] as the assertion URI
                        final NanopublicationPart assertion = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasAssertion, unusedDatasetModelNames);
                        // Specification: Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasProvenance [P] [H]', which identifies [P] as the provenance URI
                        final NanopublicationPart provenance = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasProvenance, unusedDatasetModelNames);
                        // Specification: Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasPublicationInfo [I] [H]', which identifies [I] as the publication information URI
                        final NanopublicationPart publicationInfo = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasPublicationInfo, unusedDatasetModelNames);

                        if (dialect == NanopublicationDialect.SPECIFICATION) {
                            nanopublication = SpecificationNanopublicationDialect.createNanopublicationFromParts(assertion, head, nanopublicationUri, provenance, publicationInfo);
                        } else {
                            // Don't respect the part names of non-specification dialects. Causes too many problems if the dialect differs too much from the spec.
                            // Take the part models and create a new nanopublication from scratch.
                            // Do respect the nanopublication URI. We need it to ensure the nanopublication can be updated or deleted later.
                            final NanopublicationBuilder nanopublicationBuilder = Nanopublication.builder(nanopublicationUri);

                            // Take assertions as-is
                            nanopublicationBuilder.getAssertionBuilder().setModel(assertion.getModel());

                            // Rewrite provenance statements to refer to the new assertion part URI
                            // Will do that below, once we've got the new assertion part URI
                            final Model rewrittenProvenanceModel = ModelFactory.createDefaultModel();
                            nanopublicationBuilder.getProvenanceBuilder().setModel(rewrittenProvenanceModel);

                            // Don't need to rewrite publication info, since it's only
                            nanopublicationBuilder.getPublicationInfoBuilder().setModel(publicationInfo.getModel());

                            nanopublication = nanopublicationBuilder.build();

                            // Rewrite statements of the provenance that referred to the assertion part
                            final Resource newAssertionPartResource = ResourceFactory.createResource(nanopublication.getProvenance().getName().toString());
                            final Resource oldAssertionPartResource = ResourceFactory.createResource(provenance.getName().toString());
                            for (final StmtIterator statementI = provenance.getModel().listStatements(); statementI.hasNext(); ) {
                                final Statement statement = statementI.next();
                                if (statement.getSubject().equals(oldAssertionPartResource)) {
                                    rewrittenProvenanceModel.add(newAssertionPartResource, statement.getPredicate(), statement.getObject());
                                } else {
                                    rewrittenProvenanceModel.add(statement);
                                }
                            }
                            // rewrittenProvenanceModel is already in the nanopublication
                        }
                    } catch (final MalformedNanopublicationException e) {
                        consumer.onMalformedNanopublicationException(e);
                        continue;
                    }
                }

                consumer.accept(nanopublication);
            }
        }
    }
}
