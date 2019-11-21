package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.client.TwksClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class DumpCommand extends Command {
    private final static String[] ALIASES = {};
    private final static String NAME = "dump";
    private final static Logger logger = LoggerFactory.getLogger(DumpCommand.class);
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
        try {
            client.dump();
        } catch (final IOException e) {
            logger.error("I/O exception:", e);
        }
    }

    public final static class Args {
    }
}
