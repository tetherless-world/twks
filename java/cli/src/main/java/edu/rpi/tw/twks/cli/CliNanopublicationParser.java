package edu.rpi.tw.twks.cli;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.ByteStreams;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Optional;

public final class CliNanopublicationParser {
    private final static Logger logger = LoggerFactory.getLogger(CliNanopublicationParser.class);
    private final NanopublicationDialect dialect;
    private final Optional<Lang> lang;

    public CliNanopublicationParser(final Args args) {
        NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;
        if (args.dialect != null) {
            dialect = NanopublicationDialect.valueOf(args.dialect.toUpperCase());
        }
        this.dialect = dialect;

        Optional<Lang> lang = Optional.empty();
        if (args.lang != null) {
            lang = Optional.ofNullable(RDFLanguages.shortnameToLang(args.lang));
            if (!lang.isPresent()) {
                throw new IllegalArgumentException("invalid lang " + args.lang);
            }
        }
        this.lang = lang;
    }

//    public CliNanopublicationParser(final NanopublicationDialect dialect, final Optional<Lang> lang) {
//        this.dialect = checkNotNull(dialect);
//        this.lang = checkNotNull(lang);
//    }

    private ImmutableList<Uri> getNanopublicationUris(final ImmutableList<Nanopublication> nanopublications) {
        return nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
    }

    private NanopublicationParserBuilder newNanopublicationParserBuilder() {
        final NanopublicationParserBuilder parserBuilder = NanopublicationParser.builder();
        if (dialect != null) {
            parserBuilder.setDialect(dialect);
        }
        if (lang.isPresent()) {
            parserBuilder.setLang(lang.get());
        }
        return parserBuilder;
    }

    public final ImmutableList<Nanopublication> parse(final String source) {
        if (source.equals("-")) {
            return parseStdin();
        }

        final File sourceFile = new File(source);
        if (sourceFile.isFile()) {
            return parseFile(sourceFile);
        } else if (sourceFile.isDirectory()) {
            return ImmutableList.copyOf(parseDirectory(sourceFile).values());
        }

        return parseUri(Uri.parse(source));
    }

    public final ImmutableMultimap<Path, Nanopublication> parseDirectory(File sourceDirectoryPath) {
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
                    final File file = new File(nanopublicationSubdirectory, "file");
                    resultBuilder.putAll(file.toPath(), parseFile(file));
                }
            } else {
                // Assume the directory contains a single nanopublication
                final File file = new File(sourceDirectoryPath, "file");
                resultBuilder.putAll(file.toPath(), parseFile(file));
            }
        }
        final ImmutableMultimap<Path, Nanopublication> result = resultBuilder.build();
        logger.info("parsed {} nanopublications from {}", result.size(), sourceDirectoryPath);
        return result;
    }

    public final ImmutableList<Nanopublication> parseFile(final File sourceFilePath) {
        try {
            final ImmutableList<Nanopublication> result = newNanopublicationParserBuilder().setSource(sourceFilePath).build().parseAll();
            logger.info("parsed {} nanopublications from {}: {}", result.size(), sourceFilePath, getNanopublicationUris(result));
            return result;
        } catch (final RiotNotFoundException e) {
            logger.error("nanopublication file {} not found", sourceFilePath);
            return ImmutableList.of();
        } catch (final MalformedNanopublicationException e) {
            logger.error("error parsing {}: ", sourceFilePath, e);
            return ImmutableList.of();
        }
    }

    public final ImmutableList<Nanopublication> parseStdin() {
        try {
            final byte[] trigBytes = ByteStreams.toByteArray(System.in);
            final String trigString = new String(trigBytes);
            final ImmutableList<Nanopublication> result = newNanopublicationParserBuilder().setSource(new StringReader(trigString)).build().parseAll();
            logger.info("parsed {} nanopublications from stdin: {}", result.size(), getNanopublicationUris(result));
            return result;
        } catch (final IOException | MalformedNanopublicationException e) {
            logger.error("error parsing stdin: ", e);
            return ImmutableList.of();
        }
    }

    public final ImmutableList<Nanopublication> parseUri(final Uri sourceUri) {
        try {
            final ImmutableList<Nanopublication> result = newNanopublicationParserBuilder().setSource(sourceUri).build().parseAll();
            logger.info("parsed {} nanopublications from {}: {}", result.size(), sourceUri, getNanopublicationUris(result));
            return result;
        } catch (final MalformedNanopublicationException e) {
            logger.error("error parsing {}: ", sourceUri, e);
            return ImmutableList.of();
        }
    }

    public abstract static class Args {
        @Parameter(names = {"--dialect"}, description = "dialect of the nanopublication, such as SPECIFICATION or WHYIS")
        public String dialect = null;

        @Parameter(names = {"-l", "--lang"}, description = "language/format of the nanopublication file e.g., TRIG")
        public String lang = null;
    }
}
