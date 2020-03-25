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
import java.util.Iterator;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class NanopublicationParser {
    public final static NanopublicationParser DEFAULT = new NanopublicationParser(NanopublicationDialect.SPECIFICATION, false, Optional.of(NanopublicationDialect.SPECIFICATION.getDefaultLang()));

    private final static Logger logger = LoggerFactory.getLogger(NanopublicationParser.class);
    private final NanopublicationDialect dialect;
    private final boolean ignoreMalformedNanopublications;
    private final Optional<Lang> lang;

    public NanopublicationParser(final NanopublicationDialect dialect, final boolean ignoreMalformedNanopublications, final Optional<Lang> lang) {
        this.dialect = checkNotNull(dialect);
        this.ignoreMalformedNanopublications = ignoreMalformedNanopublications;
        this.lang = checkNotNull(lang);
    }

    public final static NanopublicationParserBuilder builder() {
        return new NanopublicationParserBuilder();
    }

//    private ImmutableList<Uri> getNanopublicationUris(final ImmutableList<Nanopublication> nanopublications) {
//        return nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
//    }

    private RDFParserBuilder newRdfParserBuilder() {
        final RDFParserBuilder builder = RDFParserBuilder.create();
        builder.lang(lang.orElse(dialect.getDefaultLang()));
        return builder;
    }

    private ImmutableList<Nanopublication> parse(final RDFParser rdfParser, final Optional<Uri> sourceUri) throws MalformedNanopublicationRuntimeException {
        final Dataset dataset = DatasetFactory.create();

        try {
            rdfParser.parse(dataset);
        } catch (final RiotNotFoundException e) {
            throw e;
        } catch (final RiotException e) {
            if (ignoreMalformedNanopublications) {
                return ImmutableList.of();
            } else {
                throw new MalformedNanopublicationRuntimeException(new MalformedNanopublicationException(e));
            }
        }

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        final boolean datasetHasNamedGraphs;
        try {
            datasetHasNamedGraphs = dataset.listNames().hasNext();
        } catch (final UnsupportedOperationException e) {
            // Jena throws this exception when a graph name is a blank node
            // The latter appears to be legal TriG.
            if (ignoreMalformedNanopublications) {
                return ImmutableList.of();
            } else {
                throw new MalformedNanopublicationRuntimeException(new MalformedNanopublicationException("blank node graph names not supported"));
            }
        }
        if (datasetHasNamedGraphs) {
            return parseDataset(dataset);
        }

        final NanopublicationBuilder nanopublicationBuilder = Nanopublication.builder();
        nanopublicationBuilder.getAssertionBuilder().setModel(dataset.getDefaultModel());
        if (sourceUri.isPresent()) {
            nanopublicationBuilder.getProvenanceBuilder().wasDerivedFrom(sourceUri.get());
        }
        try {
            return ImmutableList.of(nanopublicationBuilder.build());
        } catch (final MalformedNanopublicationException e) {
            if (ignoreMalformedNanopublications) {
                return ImmutableList.of();
            } else {
                throw new MalformedNanopublicationRuntimeException(e);
            }
        }
    }

    public final ImmutableList<Nanopublication> parse(final String source) throws MalformedNanopublicationRuntimeException {
        if (source.equals("-")) {
            return parseStdin();
        }

        final File sourceFile = new File(source);
        if (sourceFile.isFile()) {
            return parseFile(sourceFile);
        } else if (sourceFile.isDirectory()) {
            return ImmutableList.copyOf(parseDirectory(sourceFile).values());
        }

        return parseUrl(Uri.parse(source));
    }

    public final ImmutableList<Nanopublication> parseDataset(final Dataset dataset) {
        final ImmutableList.Builder<Nanopublication> nanopublicationsBuilder = ImmutableList.builder();
        try (final DatasetNanopublications datasetNanopublications = new DatasetNanopublications(dataset, dialect)) {
            final Iterator<Nanopublication> nanopublicationI = datasetNanopublications.iterator();
            while (nanopublicationI.hasNext()) {
                try {
                    nanopublicationsBuilder.add(nanopublicationI.next());
                } catch (final MalformedNanopublicationRuntimeException e) {
                    if (!ignoreMalformedNanopublications) {
                        throw e;
                    }
                }
            }
        }
        return nanopublicationsBuilder.build();
    }

    public final ImmutableMultimap<Path, Nanopublication> parseDirectory(File sourceDirectoryPath) throws MalformedNanopublicationRuntimeException {
        final ImmutableMultimap.Builder<Path, Nanopublication> resultBuilder = ImmutableMultimap.builder();
        if (dialect == NanopublicationDialect.SPECIFICATION) {
            // Assume it's a directory where every .trig file is a nanopublication.
            final File[] sourceFiles = sourceDirectoryPath.listFiles();
            if (sourceFiles == null) {
                return ImmutableMultimap.of();
            }
            for (final File trigFile : sourceFiles) {
                if (!trigFile.isFile()) {
                    continue;
                }
                if (!trigFile.getName().endsWith(".trig")) {
                    continue;
                }
                resultBuilder.putAll(trigFile.toPath(), parseFile(trigFile));
            }
        } else if (dialect == NanopublicationDialect.WHYIS) {
            if (sourceDirectoryPath.getName().equals("data")) {
                sourceDirectoryPath = new File(sourceDirectoryPath, "nanopublications");
            }
            if (sourceDirectoryPath.getName().equals("nanopublications")) {
                // Trawl all of the subdirectories of /data/nanopublications
                final File[] nanopublicationSubdirectories = sourceDirectoryPath.listFiles();
                if (nanopublicationSubdirectories == null) {
                    return ImmutableMultimap.of();
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
                        resultBuilder.putAll(twksFile.toPath(), parseFile(twksFile));
                    } else {
                        final File whyisFile = new File(nanopublicationSubdirectory, "file");
                        final ImmutableList<Nanopublication> twksNanopublications = parseFile(whyisFile);
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
                        resultBuilder.putAll(whyisFile.toPath(), twksNanopublications);
                    }
                }
            } else {
                // Assume the directory contains a single nanopublication
                final File file = new File(sourceDirectoryPath, "file");
                resultBuilder.putAll(file.toPath(), parseFile(file));
            }
        }
        final ImmutableMultimap<Path, Nanopublication> result = resultBuilder.build();
        if (logger.isDebugEnabled()) {
            logger.debug("parsed {} nanopublications from {}", result.size(), sourceDirectoryPath);
        }
        return result;
    }

    public ImmutableList<Nanopublication> parseFile(final File filePath) throws MalformedNanopublicationRuntimeException {
        return parse(newRdfParserBuilder().source(filePath.getPath()).build(), Optional.of(Uri.parse(checkNotNull(filePath).toURI().toString())));
    }

    public final ImmutableList<Nanopublication> parseStdin() throws MalformedNanopublicationRuntimeException {
        final byte[] trigBytes;
        try {
            trigBytes = ByteStreams.toByteArray(System.in);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final String trigString = new String(trigBytes);
        return parseString(trigString);
    }

    public final ImmutableList<Nanopublication> parseString(final String string) throws MalformedNanopublicationRuntimeException {
        return parseString(string, Optional.empty());
    }

    public final ImmutableList<Nanopublication> parseString(final String string, final Optional<Uri> sourceUri) throws MalformedNanopublicationRuntimeException {
        return parse(newRdfParserBuilder().source(new StringReader(string)).build(), sourceUri);
    }

    public final ImmutableList<Nanopublication> parseUrl(final Uri url) throws MalformedNanopublicationRuntimeException {
        return parse(newRdfParserBuilder().source(url.toString()).build(), Optional.of(url));
    }

//    public final Nanopublication parseOne()  {
//        final ImmutableList<Nanopublication> nanopublications = parseAll();
//
//        switch (nanopublications.size()) {
//            case 0:
//                throw new IllegalStateException();
//            case 1:
//                return nanopublications.get(0);
//            default:
//                throw new MalformedNanopublicationException("more than one nanopublication parsed");
//        }
//    }
}
