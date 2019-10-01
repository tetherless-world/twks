package edu.rpi.tw.twdb.cli.command;

import com.beust.jcommander.Parameter;
import edu.rpi.tw.twdb.api.MalformedNanopublicationException;
import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationParser;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.lib.Uris;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.dmfs.rfc3986.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public final class PutNanopublicationCommand extends Command {
    private final static String NAME = "put-nanopublication";
    private final static String[] ALIASES = {"put"};
    private final static Logger logger = LoggerFactory.getLogger(PutNanopublicationCommand.class);
    private final Args args = new Args();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public void run(final Twdb db) {
        final NanopublicationParser parser = db.newNanopublicationParser();

        if (args.lang != null) {
            final Lang lang = RDFLanguages.shortnameToLang(args.lang);
            if (lang == null) {
                throw new IllegalArgumentException("invalid lang " + args.lang);
            }
            parser.setLang(lang);
        }

        final Nanopublication nanopublication;

        try {
            final File sourceFilePath = new File(args.source);
            if (sourceFilePath.isFile()) {
                nanopublication = parser.parse(sourceFilePath);
            } else {
                final Uri sourceUrl = Uris.parse(args.source);
                sourceUrl.fragment(); // Force parse
                nanopublication = parser.parse(sourceUrl);
            }
        } catch (final IOException | MalformedNanopublicationException e) {
            logger.error("error parsing {}:", args.source, e);
            return;
        }

        db.putNanopublication(nanopublication);
    }

    @Override
    public Args getArgs() {
        return args;
    }

    private final static class Args {
        @Parameter(names = {"-l", "--lang"}, description = "language/format of the nanopublication file e.g., TRIG")
        String lang = null;

        @Parameter(required = true, description = "nanopublication or assertion file path or URI")
        String source = null;
    }
}
