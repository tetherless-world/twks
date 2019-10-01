package edu.rpi.tw.twdb.cli.command;

import com.beust.jcommander.Parameter;
import edu.rpi.tw.twdb.api.Twdb;

public final class PutCommand extends Command {
    private final Args args = new Args();

    @Override
    public String getName() {
        return "put";
    }

    @Override
    public void run(final Twdb db) {

    }

    @Override
    public Args getArgs() {
        return args;
    }

    private final static class Args {
        @Parameter(required = true)
        String nanopublicationFilePath = null;
    }
}
