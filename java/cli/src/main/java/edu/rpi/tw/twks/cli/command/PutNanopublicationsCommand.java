package edu.rpi.tw.twks.cli.command;

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
import java.util.ArrayList;
import java.util.List;

public final class PutNanopublicationsCommand extends Command {
    private final static String NAME = "put-nanopublications";
    private final static String[] ALIASES = {"put"};
    private final static Logger logger = LoggerFactory.getLogger(PutNanopublicationsCommand.class);
    private final Args args = new Args();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    private NanopublicationParser newParser(final NanopublicationDialect dialect, final Lang lang) {
        final NanopublicationParser parser = new NanopublicationParser();
        if (dialect != null) {
            parser.setDialect(dialect);
            if (dialect == NanopublicationDialect.WHYIS) {
                parser.setLang(Lang.TRIG);
            }
        }
        if (lang != null) {
            parser.setLang(lang);
        }
        return parser;
    }

    private List<Nanopublication> parseSourceDirectory(final NanopublicationDialect dialect, final Lang lang, File sourceDirectoryPath) throws IOException, MalformedNanopublicationException {
        final List<Nanopublication> nanopublications = new ArrayList<>();
        switch (dialect) {
            case SPECIFICATION: {
                // Assume it's a directory where every .trig file is a nanopublication.
                final File[] sourceFiles = sourceDirectoryPath.listFiles();
                if (sourceFiles != null) {
                    for (final File trigFile : sourceFiles) {
                        if (!trigFile.isFile()) {
                            continue;
                        }
                        if (!trigFile.getName().endsWith(".trig")) {
                            continue;
                        }
                        nanopublications.add(newParser(dialect, lang).parse(trigFile));
                    }
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
                    if (nanopublicationSubdirectories != null) {
                        for (final File nanopublicationSubdirectory : nanopublicationSubdirectories) {
                            if (!nanopublicationSubdirectory.isDirectory()) {
                                continue;
                            }
                            nanopublications.add(newParser(dialect, lang).parse(new File(nanopublicationSubdirectory, "file")));
                        }
                    }
                } else {
                    // Assume the directory contains a single nanopublication
                    nanopublications.add(newParser(dialect, lang).parse(new File(sourceDirectoryPath, "file")));
                }
                break;
            }
        }
        return nanopublications;
    }

    @Override
    public void run(final Apis apis) {
        NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;
        if (args.dialect != null) {
            dialect = NanopublicationDialect.valueOf(args.dialect.toUpperCase());
        }

        Lang lang = null;
        if (args.lang != null) {
            lang = RDFLanguages.shortnameToLang(args.lang);
            if (lang == null) {
                throw new IllegalArgumentException("invalid lang " + args.lang);
            }
        }

        final List<Nanopublication> nanopublications;

        try {
            if (args.source != null) {
                final File sourceFile = new File(args.source);
                if (sourceFile.isFile()) {
                    nanopublications = ImmutableList.of(newParser(dialect, lang).parse(sourceFile));
                } else if (sourceFile.isDirectory()) {
                    nanopublications = parseSourceDirectory(dialect, lang, sourceFile);
                } else {
                    nanopublications = ImmutableList.of(newParser(dialect, lang).parse(Uri.parse(args.source)));
                }
            } else {
                final byte[] trigBytes = ByteStreams.toByteArray(System.in);
                final String trigString = new String(trigBytes);
                nanopublications = ImmutableList.of(newParser(dialect, lang).parse(new StringReader(trigString)));
            }
        } catch (final IOException | MalformedNanopublicationException e) {
            logger.error("error parsing {}:", args.source, e);
            return;
        }

        logger.info("parsed {} nanopublication(s) from {}", nanopublications.size(), args.source);

        for (int nanopublicationI = 0; nanopublicationI < nanopublications.size(); nanopublicationI++) {
            final Nanopublication nanopublication = nanopublications.get(nanopublicationI);
            apis.getNanopublicationCrudApi().putNanopublication(nanopublication);
            if (nanopublicationI > 0 && (nanopublicationI + 1) % 10 == 0) {
                logger.info("put {} nanopublication(s) from {}", nanopublicationI + 1, args.source);
            }
        }

        logger.info("put {} nanopublication(s) from {}", nanopublications.size(), args.source);
    }

    @Override
    public Args getArgs() {
        return args;
    }

    private final static class Args {
        @Parameter(names = {"--dialect"}, description = "dialect of the nanopublication, such as SPECIFICATION or WHYIS")
        String dialect = null;

        @Parameter(names = {"-l", "--lang"}, description = "language/format of the nanopublication file e.g., TRIG")
        String lang = null;

        @Parameter(names = {"-f"}, description = "nanopublication or assertion file path or URI")
        String source = null;
    }
}
