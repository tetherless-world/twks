package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.configuration.TwksConfiguration;

public abstract class QuadStoreTwks<TwksConfigurationT extends TwksConfiguration, TwksMetricsT extends AbstractTwksMetrics> extends AbstractTwks<TwksConfigurationT, TwksMetricsT> {
    protected QuadStoreTwks(final TwksConfigurationT configuration, final TwksMetricsT metrics) {
        super(configuration, metrics);
    }
}
