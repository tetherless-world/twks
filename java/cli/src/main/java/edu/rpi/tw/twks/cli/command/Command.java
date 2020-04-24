package edu.rpi.tw.twks.cli.command;

import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.api.TwksClient;

public abstract class Command {
    public String[] getAliases() {
        return new String[0];
    }

    public abstract Object getArgs();

    public abstract String getName();

    public abstract void run(TwksClient client, MetricRegistry metricRegistry);
}
