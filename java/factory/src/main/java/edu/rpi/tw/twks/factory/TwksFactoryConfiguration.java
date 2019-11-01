package edu.rpi.tw.twks.factory;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.AbstractConfiguration;
import edu.rpi.tw.twks.tdb.Tdb2TwksConfiguration;

import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class TwksFactoryConfiguration extends AbstractConfiguration<TwksFactoryConfiguration> {
    private final Optional<Tdb2TwksConfiguration> tdb2Configuration;

    private TwksFactoryConfiguration(final Optional<Tdb2TwksConfiguration> tdb2Configuration) {
        this.tdb2Configuration = checkNotNull(tdb2Configuration);
        if (tdb2Configuration.isPresent()) {
            checkState(!tdb2Configuration.get().isEmpty());
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Optional<Tdb2TwksConfiguration> getTdb2Configuration() {
        return tdb2Configuration;
    }

    public final boolean isEmpty() {
        if (getTdb2Configuration().isPresent()) {
            return false;
        }
        return true;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("tdb2Configuration", tdb2Configuration.orElse(null));
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, TwksFactoryConfiguration> {
        private Optional<Tdb2TwksConfiguration> tdb2Configuration = Optional.empty();

        protected Builder() {
        }

        @Override
        public final TwksFactoryConfiguration build() {
            return new TwksFactoryConfiguration(tdb2Configuration);
        }

        public final Optional<Tdb2TwksConfiguration> getTdb2Configuration() {
            return tdb2Configuration;
        }

        public final Builder setTdb2Configuration(final Optional<Tdb2TwksConfiguration> tdb2Configuration) {
            this.tdb2Configuration = checkNotNull(tdb2Configuration);
            return this;
        }

        public final Builder setTdb2Configuration(final Tdb2TwksConfiguration tdb2Configuration) {
            this.tdb2Configuration = Optional.of(tdb2Configuration);
            return this;
        }

        @Override
        public final Builder setFromProperties(final Properties properties) {
            final Tdb2TwksConfiguration tdb2Configuration = Tdb2TwksConfiguration.builder().setFromProperties(properties).build();
            if (!tdb2Configuration.isEmpty()) {
                this.tdb2Configuration = Optional.of(tdb2Configuration);
            }
            return this;
        }

        @Override
        public final Builder setFromSystemProperties() {
            return (Builder) super.setFromSystemProperties();
        }
    }
}
