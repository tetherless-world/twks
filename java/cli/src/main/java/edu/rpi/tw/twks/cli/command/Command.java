package edu.rpi.tw.twks.cli.command;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.cli.GlobalArgs;

public abstract class Command {
    public String[] getAliases() {
        return new String[0];
    }

    public abstract GlobalArgs getArgs();

    public abstract String getName();

    public abstract void run(TwksClient client, MetricRegistry metricRegistry);
}
