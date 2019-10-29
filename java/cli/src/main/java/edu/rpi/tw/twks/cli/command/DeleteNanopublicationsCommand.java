package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class DeleteNanopublicationsCommand extends Command {
    private final static String[] ALIASES = {"delete"};
    private final static String NAME = "delete-nanopublications";
    private final static Logger logger = LoggerFactory.getLogger(DeleteNanopublicationsCommand.class);
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
    public void run(final Apis apis) {
        final ImmutableList<Uri> nanopublicationUris = args.nanopublicationUris.stream().map(uri -> Uri.parse(uri)).collect(ImmutableList.toImmutableList());
        apis.getNanopublicationCrudApi().deleteNanopublications(nanopublicationUris);
        logger.info("deleted {} nanopublication(s)", nanopublicationUris.size());
    }

    public final static class Args {
        @Parameter(required = true, description = "1+ nanopublication URI(s)")
        List<String> nanopublicationUris = new ArrayList<>();
    }
}
