package edu.rpi.tw.twks.cli.command;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.cli.GlobalArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class DumpCommand extends Command {
    private final static String[] ALIASES = {};
    private final static String NAME = "dump";
    private final static Logger logger = LoggerFactory.getLogger(DumpCommand.class);
    private final GlobalArgs args = new GlobalArgs();

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public GlobalArgs getArgs() {
        return args;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void run(final TwksClient client, final MetricRegistry metricRegistry) {
        try {
            client.dump();
        } catch (final IOException e) {
            logger.error("I/O exception:", e);
        }
    }
}
