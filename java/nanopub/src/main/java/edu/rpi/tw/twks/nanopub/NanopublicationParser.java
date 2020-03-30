package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Nanopublication parser. Parse methods are of the form:
 * <p>
 * parseX(source, sink) -> void
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

    private void parse(final RDFParser rdfParser, final NanopublicationParserSink sink, final Optional<Uri> sourceUri) {
        final Dataset dataset = DatasetFactory.create();

        try {
            rdfParser.parse(dataset);
        } catch (final RiotNotFoundException e) {
            throw e;
        } catch (final RiotException e) {
            sink.onMalformedNanopublicationException(new MalformedNanopublicationException(e));
            return;
        }

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        final boolean datasetHasNamedGraphs;
        try {
            datasetHasNamedGraphs = dataset.listNames().hasNext();
        } catch (final UnsupportedOperationException e) {
            // Jena throws this exception when a graph name is a blank node
            // The latter appears to be legal TriG.
            sink.onMalformedNanopublicationException(new MalformedNanopublicationException("blank node graph names not supported"));
            return;
        }
        if (datasetHasNamedGraphs) {
            parseDataset(dataset, sink);
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
            sink.onMalformedNanopublicationException(e);
            return;
        }
        sink.accept(nanopublication);
    }

    public final ImmutableList<Nanopublication> parse(final String source) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationParserSink sink = new CollectingNanopublicationParserSink();
        parse(source, sink);
        return sink.build();
    }

    public final void parse(final String source, final NanopublicationParserSink sink) {
        if (source.equals("-")) {
            parseStdin(sink);
            return;
        }

        final File sourceFile = new File(source);
        if (sourceFile.isFile()) {
            parseFile(sourceFile.toPath(), sink);
            return;
        } else if (sourceFile.isDirectory()) {
            new NanopublicationDirectoryParser(this).parseDirectory(sourceFile, new NanopublicationDirectoryParserSink() {
                @Override
                public void accept(final Nanopublication nanopublication, final Path nanopublicationFilePath) {
                    sink.accept(nanopublication);
                }

                @Override
                public void onMalformedNanopublicationException(final MalformedNanopublicationException exception, final Path nanopublicationFilePath) {
                    sink.onMalformedNanopublicationException(exception);
                }
            });
        }

        parseUrl(Uri.parse(source));
    }

    private void parseDataset(final Dataset dataset, final NanopublicationParserSink sink) {
        try (final DatasetNanopublications datasetNanopublications = new DatasetNanopublications(dataset, dialect)) {
            final Iterator<Nanopublication> nanopublicationI = datasetNanopublications.iterator();
            while (nanopublicationI.hasNext()) {
                final Nanopublication nanopublication;
                try {
                    nanopublication = nanopublicationI.next();
                } catch (final MalformedNanopublicationRuntimeException e) {
                    sink.onMalformedNanopublicationException(e.getCause());
                    continue;
                }
                sink.accept(nanopublication);
            }
        }
    }

    public ImmutableList<Nanopublication> parseFile(final Path filePath) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationParserSink sink = new CollectingNanopublicationParserSink();
        parseFile(filePath, sink);
        return sink.build();
    }

    public void parseFile(final Path filePath, final NanopublicationParserSink sink) {
        parse(newRdfParserBuilder().source(filePath).build(), sink, Optional.of(Uri.parse(checkNotNull(filePath).toUri().toString())));
    }

    public final ImmutableList<Nanopublication> parseStdin() throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationParserSink sink = new CollectingNanopublicationParserSink();
        parseStdin(sink);
        return sink.build();
    }

    public final void parseStdin(final NanopublicationParserSink sink) {
        final byte[] trigBytes;
        try {
            trigBytes = ByteStreams.toByteArray(System.in);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final String trigString = new String(trigBytes);
        parseString(trigString, sink);
    }

    public final ImmutableList<Nanopublication> parseString(final String string) throws MalformedNanopublicationRuntimeException {
        return parseString(string, Optional.empty());
    }

    public final void parseString(final String string, final NanopublicationParserSink sink) {
        parseString(string, sink, Optional.empty());
    }

    public final ImmutableList<Nanopublication> parseString(final String string, final Optional<Uri> sourceUri) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationParserSink sink = new CollectingNanopublicationParserSink();
        parseString(string, sink, sourceUri);
        return sink.build();
    }

    public final void parseString(final String string, final NanopublicationParserSink sink, final Optional<Uri> sourceUri) {
        parse(newRdfParserBuilder().source(new StringReader(string)).build(), sink, sourceUri);
    }

    public final ImmutableList<Nanopublication> parseUrl(final Uri url) throws MalformedNanopublicationRuntimeException {
        final CollectingNanopublicationParserSink sink = new CollectingNanopublicationParserSink();
        parseUrl(url, sink);
        return sink.build();
    }

    public final void parseUrl(final Uri url, final NanopublicationParserSink sink) {
        parse(newRdfParserBuilder().source(url.toString()).build(), sink, Optional.of(url));
    }

    private final static class CollectingNanopublicationParserSink implements NanopublicationParserSink {
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
}
