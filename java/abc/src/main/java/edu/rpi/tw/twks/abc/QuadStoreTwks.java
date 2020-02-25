package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.configuration.TwksConfiguration;

public abstract class QuadStoreTwks<TwksConfigurationT extends TwksConfiguration> extends AbstractTwks<TwksConfigurationT> {
    protected QuadStoreTwks(final TwksConfigurationT configuration) {
        super(configuration);
    }
}
