package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.client.TwksClient;

public abstract class Command {
    public String[] getAliases() {
        return new String[0];
    }

    public abstract Object getArgs();

    public abstract String getName();

    public abstract void run(TwksClient client);
}
