package edu.rpi.tw.twks.factory;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.TwksConfiguration;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwksFactoryConfiguration extends TwksConfiguration {
    private final Optional<String> tdb2Location;

    protected TwksFactoryConfiguration(final Path dumpDirectoryPath, final Optional<String> tdb2Location) {
        super(dumpDirectoryPath);
        this.tdb2Location = checkNotNull(tdb2Location);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Optional<String> getTdb2Location() {
        return tdb2Location;
    }

    public final boolean isEmpty() {
        return !getTdb2Location().isPresent();
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("tdb2Location", tdb2Location.orElse(null));
    }

    public static class Builder extends TwksConfiguration.Builder {
        private Optional<String> tdb2Location = Optional.empty();

        protected Builder() {
        }

        @Override
        public Builder setDumpDirectoryPath(final Path dumpDirectoryPath) {
            return (Builder) super.setDumpDirectoryPath(dumpDirectoryPath);
        }

        @Override
        public TwksFactoryConfiguration build() {
            return new TwksFactoryConfiguration(getDumpDirectoryPath(), tdb2Location);
        }

        public final Optional<String> getTdb2Location() {
            return tdb2Location;
        }

        public final Builder setTdb2Location(final Optional<String> tdb2Location) {
            this.tdb2Location = checkNotNull(tdb2Location);
            return this;
        }

        @Override
        public Builder setFromSystemProperties() {
            return (Builder) super.setFromSystemProperties();
        }

        @Override
        public Builder setFromProperties(final Properties properties) {
            super.setFromProperties(properties);

            @Nullable final String tdb2Location = properties.getProperty(PropertyKeys.TDB2_LOCATION);
            if (tdb2Location != null) {
                setTdb2Location(Optional.of(tdb2Location));
            }

            return this;
        }
    }

    public static class PropertyKeys extends TwksConfiguration.PropertyKeys {
        public final static String TDB2_LOCATION = "twks.tdbLocation";
    }
}
