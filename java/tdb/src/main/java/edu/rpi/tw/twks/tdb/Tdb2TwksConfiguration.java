package edu.rpi.tw.twks.tdb;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.TwksConfiguration;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class Tdb2TwksConfiguration extends TwksConfiguration {
    private final Optional<String> location;

    protected Tdb2TwksConfiguration(final Builder builder) {
        super(builder);
        this.location = builder.getLocation();
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Optional<String> getLocation() {
        return location;
    }

    @Override
    protected final MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("tdb2Location", location.orElse(null));
    }

    public final static class Builder extends TwksConfiguration.Builder<Builder, Tdb2TwksConfiguration> {
        private Optional<String> location = Optional.empty();

        protected Builder() {
        }

        @Override
        public Tdb2TwksConfiguration build() {
            return new Tdb2TwksConfiguration(this);
        }

        public final Optional<String> getLocation() {
            return location;
        }

        public final Builder setLocation(final Optional<String> location) {
            this.location = checkNotNull(location);
            markDirty();
            return this;
        }

        @Override
        public final Builder setFromProperties(final PropertiesWrapper properties) {
            properties.getString(PropertyDefinitions.LOCATION).ifPresent(value -> setLocation(Optional.of(value)));
            return super.setFromProperties(properties);
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinition LOCATION = new PropertyDefinition("tdbLocation");
    }
}
