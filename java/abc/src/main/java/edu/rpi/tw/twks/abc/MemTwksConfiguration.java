package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.api.TwksGraphNameCacheConfiguration;

import java.nio.file.Path;

public final class MemTwksConfiguration extends TwksConfiguration {
    private MemTwksConfiguration(final Path dumpDirectoryPath, final TwksGraphNameCacheConfiguration graphNameCacheConfiguration) {
        super(dumpDirectoryPath, graphNameCacheConfiguration);
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final static class Builder extends TwksConfiguration.Builder<Builder, MemTwksConfiguration> {
        @Override
        public final MemTwksConfiguration build() {
            return new MemTwksConfiguration(getDumpDirectoryPath(), getGraphNameCacheConfiguration());
        }
    }
}
