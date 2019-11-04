package edu.rpi.tw.twks.factory;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.agraph.AllegroGraphTwksConfiguration;
import edu.rpi.tw.twks.api.AbstractConfiguration;
import edu.rpi.tw.twks.tdb.Tdb2TwksConfiguration;

import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class TwksFactoryConfiguration extends AbstractConfiguration {
    private final Optional<AllegroGraphTwksConfiguration> allegroGraphConfiguration;
    private final Optional<Tdb2TwksConfiguration> tdb2Configuration;

    private TwksFactoryConfiguration(final Optional<AllegroGraphTwksConfiguration> allegroGraphConfiguration, final Optional<Tdb2TwksConfiguration> tdb2Configuration) {
        this.allegroGraphConfiguration = checkNotNull(allegroGraphConfiguration);
        this.tdb2Configuration = checkNotNull(tdb2Configuration);
        if (tdb2Configuration.isPresent()) {
            checkState(!tdb2Configuration.get().isEmpty());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Optional<AllegroGraphTwksConfiguration> getAllegroGraphConfiguration() {
        return allegroGraphConfiguration;
    }

    public final Optional<Tdb2TwksConfiguration> getTdb2Configuration() {
        return tdb2Configuration;
    }

    public final boolean isEmpty() {
        if (getAllegroGraphConfiguration().isPresent()) {
            return false;
        }
        if (getTdb2Configuration().isPresent()) {
            return false;
        }
        return true;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("allegroGraphConfiguration", allegroGraphConfiguration.orElse(null))
                .add("tdb2Configuration", tdb2Configuration.orElse(null));
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, TwksFactoryConfiguration> {
        private Optional<AllegroGraphTwksConfiguration> allegroGraphConfiguration = Optional.empty();
        private Optional<Tdb2TwksConfiguration> tdb2Configuration = Optional.empty();

        protected Builder() {
        }

        @Override
        public final TwksFactoryConfiguration build() {
            return new TwksFactoryConfiguration(allegroGraphConfiguration, tdb2Configuration);
        }

        public final Optional<AllegroGraphTwksConfiguration> getAllegroGraphConfiguration() {
            return allegroGraphConfiguration;
        }

        public final Builder setAllegroGraphConfiguration(final Optional<AllegroGraphTwksConfiguration> allegroGraphConfiguration) {
            this.allegroGraphConfiguration = checkNotNull(allegroGraphConfiguration);
            return this;
        }

        public final Builder setAllegroGraphConfiguration(final AllegroGraphTwksConfiguration allegroGraphConfiguration) {
            return setAllegroGraphConfiguration(Optional.of(allegroGraphConfiguration));
        }

        public final Optional<Tdb2TwksConfiguration> getTdb2Configuration() {
            return tdb2Configuration;
        }

        public final Builder setTdb2Configuration(final Optional<Tdb2TwksConfiguration> tdb2Configuration) {
            this.tdb2Configuration = checkNotNull(tdb2Configuration);
            return this;
        }

        public final Builder setTdb2Configuration(final Tdb2TwksConfiguration tdb2Configuration) {
            return setTdb2Configuration(Optional.of(tdb2Configuration));
        }

        @Override
        public final Builder setFromProperties(final Properties properties) {
            {
                final AllegroGraphTwksConfiguration.Builder allegroGraphConfigurationBuilder = AllegroGraphTwksConfiguration.builder().setFromProperties(properties);
                if (allegroGraphConfigurationBuilder.isValid()) {
                    setAllegroGraphConfiguration(allegroGraphConfigurationBuilder.build());
                }
            }

            {
                final Tdb2TwksConfiguration tdb2Configuration = Tdb2TwksConfiguration.builder().setFromProperties(properties).build();
                if (!tdb2Configuration.isEmpty()) {
                    setTdb2Configuration(tdb2Configuration);
                }
            }

            return this;
        }
    }
}
