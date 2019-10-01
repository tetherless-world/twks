package edu.rpi.tw.twdb.cli.command;

import edu.rpi.tw.twdb.api.Twdb;

public abstract class Command {
    public abstract Object getArgs();

    public abstract String getName();

    public abstract void run(Twdb db);
}
