package edu.rpi.tw.twks.factory;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.agraph.AllegroGraphTwksConfiguration;
import edu.rpi.tw.twks.configuration.AbstractConfiguration;
import edu.rpi.tw.twks.tdb.Tdb2TwksConfiguration;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwksFactoryConfiguration extends AbstractConfiguration {
    private final Optional<AllegroGraphTwksConfiguration> allegroGraphConfiguration;
    private final Optional<Tdb2TwksConfiguration> tdb2Configuration;

    private TwksFactoryConfiguration(final Builder builder) {
        this.allegroGraphConfiguration = builder.getAllegroGraphConfiguration();
        this.tdb2Configuration = builder.getTdb2Configuration();
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
            return new TwksFactoryConfiguration(this);
        }

        public final Optional<AllegroGraphTwksConfiguration> getAllegroGraphConfiguration() {
            return allegroGraphConfiguration;
        }

        public final Builder setAllegroGraphConfiguration(final Optional<AllegroGraphTwksConfiguration> allegroGraphConfiguration) {
            this.allegroGraphConfiguration = checkNotNull(allegroGraphConfiguration);
            markDirty();
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
            markDirty();
            return this;
        }

        public final Builder setTdb2Configuration(final Tdb2TwksConfiguration tdb2Configuration) {
            return setTdb2Configuration(Optional.of(tdb2Configuration));
        }

        @Override
        public final Builder set(final ConfigurationWrapper properties) {
            {
                final AllegroGraphTwksConfiguration.Builder allegroGraphConfigurationBuilder = AllegroGraphTwksConfiguration.builder().set(properties);
                if (allegroGraphConfigurationBuilder.isDirty() && allegroGraphConfigurationBuilder.isValid()) {
                    setAllegroGraphConfiguration(allegroGraphConfigurationBuilder.build());
                }
            }

            {
                final Tdb2TwksConfiguration.Builder tdb2ConfigurationBuilder = Tdb2TwksConfiguration.builder().set(properties);
                if (tdb2ConfigurationBuilder.isDirty()) {
                    setTdb2Configuration(tdb2ConfigurationBuilder.build());
                }
            }

            return this;
        }
    }
}
