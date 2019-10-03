package edu.rpi.tw.twdb.cli.command;

import com.beust.jcommander.Parameter;
import edu.rpi.tw.nanopub.*;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.dmfs.rfc3986.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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

    @Override
    public void run(final Twdb db) {
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

        final List<Nanopublication> nanopublications = new ArrayList<>();

        try {
            final File sourceFile = new File(args.source);
            if (sourceFile.isFile()) {
                nanopublications.add(newParser(dialect, lang).parse(sourceFile));
            } else if (sourceFile.isDirectory()) {
                switch (dialect) {
                    case SPECIFICATION: {
                        // Assume it's a directory where every .trig file is a nanopublication.
                        final File[] sourceFiles = sourceFile.listFiles();
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
                        File sourceDirectory = sourceFile;
                        if (sourceDirectory.getName().equals("data")) {
                            sourceDirectory = new File(sourceFile, "nanopublications");
                        }
                        if (sourceDirectory.getName().equals("nanopublications")) {
                            // Trawl all of the subdirectories of /data/nanopublications
                            final File[] nanopublicationSubdirectories = sourceDirectory.listFiles();
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
                            nanopublications.add(newParser(dialect, lang).parse(new File(sourceDirectory, "file")));
                        }
                        break;
                    }
                }
            } else {
                final Uri sourceUrl = Uris.parse(args.source);
                sourceUrl.fragment(); // Force parse
                nanopublications.add(newParser(dialect, lang).parse(sourceUrl));
            }
        } catch (final IOException | MalformedNanopublicationException e) {
            logger.error("error parsing {}:", args.source, e);
            return;
        }

        logger.info("parsed {} nanopublication(s) from {}", nanopublications.size(), args.source);

        try (final TwdbTransaction transaction = db.beginTransaction(ReadWrite.WRITE)) {
            for (int nanopublicationI = 0; nanopublicationI < nanopublications.size(); nanopublicationI++) {
                final Nanopublication nanopublication = nanopublications.get(nanopublicationI);
                db.putNanopublication(nanopublication, transaction);
                if (nanopublicationI > 0 && (nanopublicationI + 1) % 10 == 0) {
                    logger.info("put {} nanopublication(s) from {}", nanopublicationI + 1, args.source);
                }
            }
            transaction.commit();
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

        @Parameter(required = true, description = "nanopublication or assertion file path or URI")
        String source = null;
    }
}
