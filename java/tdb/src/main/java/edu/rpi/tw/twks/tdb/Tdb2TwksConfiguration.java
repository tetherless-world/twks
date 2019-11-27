package edu.rpi.tw.twks.tdb;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.api.TwksGraphNameCacheConfiguration;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class Tdb2TwksConfiguration extends TwksConfiguration {
    private final Optional<String> location;

    protected Tdb2TwksConfiguration(final Path dumpDirectoryPath, final TwksGraphNameCacheConfiguration graphNameCacheConfiguration, final Optional<String> location) {
        super(dumpDirectoryPath, graphNameCacheConfiguration);
        this.location = checkNotNull(location);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Optional<String> getLocation() {
        return location;
    }

    public final boolean isEmpty() {
        return !getLocation().isPresent();
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("tdb2Location", location.orElse(null));
    }

    public static class Builder extends TwksConfiguration.Builder<Builder, Tdb2TwksConfiguration> {
        private Optional<String> location = Optional.empty();

        protected Builder() {
        }

        @Override
        public Tdb2TwksConfiguration build() {
            return new Tdb2TwksConfiguration(getDumpDirectoryPath(), getGraphNameCacheConfiguration(), location);
        }

        public final Optional<String> getLocation() {
            return location;
        }

        public final Builder setLocation(final Optional<String> location) {
            this.location = checkNotNull(location);
            return this;
        }

        @Override
        public Builder setFromProperties(final Properties properties) {
            super.setFromProperties(properties);

            @Nullable final String location = properties.getProperty(FieldDefinitions.LOCATION.getPropertyKey());
            if (location != null) {
                setLocation(Optional.of(location));
            }

            return this;
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinition LOCATION = new ConfigurationFieldDefinition("twks.tdbLocation");
    }
}
