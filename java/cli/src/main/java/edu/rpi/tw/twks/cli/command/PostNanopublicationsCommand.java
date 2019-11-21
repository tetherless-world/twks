package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.cli.CliNanopublicationParser;
import edu.rpi.tw.twks.client.TwksClient;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class PostNanopublicationsCommand extends Command {
    private final static String[] ALIASES = {"post", "put", "put-nanopublications"};
    private final static String NAME = "post-nanopublications";
    private final static Logger logger = LoggerFactory.getLogger(PostNanopublicationsCommand.class);
    private final Args args = new Args();

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public Args getArgs() {
        return args;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void run(final TwksClient client) {
        final CliNanopublicationParser parser = new CliNanopublicationParser(args);

        final ImmutableList<Nanopublication> nanopublications;

        if (args.sources.isEmpty()) {
            nanopublications = parser.parseStdin();
        } else {
            final ImmutableList.Builder<Nanopublication> nanopublicationsBuilder = ImmutableList.builder();
            for (final String source : args.sources) {
                nanopublicationsBuilder.addAll(parser.parse(source));
            }
            nanopublications = nanopublicationsBuilder.build();
        }

        logger.info("parsed {} nanopublication(s) total", nanopublications.size());

        client.postNanopublications(nanopublications);

        logger.info("post {} nanopublication(s) total", nanopublications.size());
    }

    public final static class Args extends CliNanopublicationParser.Args {
        @Parameter(required = true, description = "1+ nanopublication or assertion file path(s) or URI(s), or - for stdin")
        List<String> sources = new ArrayList<>();
    }
}
