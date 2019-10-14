package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.SparqlQueryApi;

public abstract class Command {
    public abstract Object getArgs();

    public String[] getAliases() {
        return new String[0];
    }

    public abstract String getName();

    public abstract void run(NanopublicationCrudApi nanopublicationCrudApi, SparqlQueryApi sparqlQueryApi);
}
