package edu.rpi.tw.twks.cli;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationDialect;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

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

    public CliNanopublicationParser(final NanopublicationDialect dialect, final Optional<Lang> lang) {
        this.dialect = checkNotNull(dialect);
        this.lang = checkNotNull(lang);
    }

    private NanopublicationParser newNanopublicationParser() {
        final NanopublicationParser parser = new NanopublicationParser();
        if (dialect != null) {
            parser.setDialect(dialect);
            if (dialect == NanopublicationDialect.WHYIS) {
                parser.setLang(Lang.TRIG);
            }
        }
        if (lang.isPresent()) {
            parser.setLang(lang.get());
        }
        return parser;
    }

    public final ImmutableList<Nanopublication> parse(final String source) {
        if (source.equals("-")) {
            return parseStdin();
        }

        final File sourceFile = new File(source);
        if (sourceFile.isFile()) {
            return parseFile(sourceFile);
        } else if (sourceFile.isDirectory()) {
            return parseDirectory(sourceFile);
        }

        return parseUri(Uri.parse(source));
    }

    public final ImmutableList<Nanopublication> parseDirectory(File sourceDirectoryPath) {
        final ImmutableList.Builder<Nanopublication> resultBuilder = ImmutableList.builder();
        switch (dialect) {
            case SPECIFICATION: {
                // Assume it's a directory where every .trig file is a nanopublication.
                final File[] sourceFiles = sourceDirectoryPath.listFiles();
                if (sourceFiles == null) {
                    return ImmutableList.of();
                }
                for (final File trigFile : sourceFiles) {
                    if (!trigFile.isFile()) {
                        continue;
                    }
                    if (!trigFile.getName().endsWith(".trig")) {
                        continue;
                    }
                    resultBuilder.addAll(parseFile(trigFile));
                }

                break;
            }
            case WHYIS: {
                if (sourceDirectoryPath.getName().equals("data")) {
                    sourceDirectoryPath = new File(sourceDirectoryPath, "nanopublications");
                }
                if (sourceDirectoryPath.getName().equals("nanopublications")) {
                    // Trawl all of the subdirectories of /data/nanopublications
                    final File[] nanopublicationSubdirectories = sourceDirectoryPath.listFiles();
                    if (nanopublicationSubdirectories == null) {
                        return ImmutableList.of();
                    }

                    for (final File nanopublicationSubdirectory : nanopublicationSubdirectories) {
                        if (!nanopublicationSubdirectory.isDirectory()) {
                            continue;
                        }
                        resultBuilder.addAll(parseFile(new File(nanopublicationSubdirectory, "file")));
                    }
                } else {
                    // Assume the directory contains a single nanopublication
                    resultBuilder.addAll(parseFile(new File(sourceDirectoryPath, "file")));
                }
                break;
            }
        }
        final ImmutableList<Nanopublication> result = resultBuilder.build();
        logger.info("parsed {} nanopublications from {}", result.size(), sourceDirectoryPath);
        return result;
    }

    public final ImmutableList<Nanopublication> parseFile(final File sourceFilePath) {
        try {
            final ImmutableList<Nanopublication> result = newNanopublicationParser().parseAll(sourceFilePath);
            logger.info("parsed {} nanopublications from {}", result.size(), sourceFilePath);
            return result;
        } catch (final MalformedNanopublicationException e) {
            logger.error("error parsing {}: ", sourceFilePath, e);
            return ImmutableList.of();
        }
    }

    public final ImmutableList<Nanopublication> parseStdin() {
        try {
            final byte[] trigBytes = ByteStreams.toByteArray(System.in);
            final String trigString = new String(trigBytes);
            final ImmutableList<Nanopublication> result = newNanopublicationParser().parseAll(new StringReader(trigString));
            logger.info("parsed {} nanopublications from stdin", result.size());
            return result;
        } catch (final IOException | MalformedNanopublicationException e) {
            logger.error("error parsing stdin: ", e);
            return ImmutableList.of();
        }
    }

    public final ImmutableList<Nanopublication> parseUri(final Uri sourceUri) {
        try {
            final ImmutableList<Nanopublication> result = newNanopublicationParser().parseAll(sourceUri);
            logger.info("parsed {} nanopublications from {}", result.size(), sourceUri);
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
