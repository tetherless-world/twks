package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;

import java.nio.file.Path;

public final class MemTwksConfiguration extends TwksConfiguration {
    private MemTwksConfiguration(final Path dumpDirectoryPath) {
        super(dumpDirectoryPath);
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final static class Builder extends TwksConfiguration.Builder<Builder, MemTwksConfiguration> {
        @Override
        public final MemTwksConfiguration build() {
            return new MemTwksConfiguration(getDumpDirectoryPath());
        }
    }
}
