package edu.rpi.tw.twks.mem;

import edu.rpi.tw.twks.api.TwksConfiguration;

public final class MemTwksConfiguration extends TwksConfiguration {
    private MemTwksConfiguration(final Builder builder) {
        super(builder);
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final static class Builder extends TwksConfiguration.Builder<Builder, MemTwksConfiguration> {
        @Override
        public final MemTwksConfiguration build() {
            return new MemTwksConfiguration(this);
        }
    }
}
