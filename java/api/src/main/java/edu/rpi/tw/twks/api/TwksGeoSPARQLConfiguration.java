package edu.rpi.tw.twks.api;

import javax.annotation.Nullable;
import java.util.Properties;

public final class TwksGeoSPARQLConfiguration extends AbstractConfiguration {
    private final boolean enable;

    private TwksGeoSPARQLConfiguration(final Builder builder) {
        this.enable = builder.getEnable();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final boolean getEnable() {
        return enable;
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, TwksGeoSPARQLConfiguration> {
        private boolean enable = FieldDefinitions.ENABLE.getDefault();

        @Override
        public TwksGeoSPARQLConfiguration build() {
            return new TwksGeoSPARQLConfiguration(this);
        }

        public final boolean getEnable() {
            return enable;
        }

        public final Builder setEnable(final boolean enable) {
            this.enable = enable;
            return this;
        }

        @Override
        public Builder setFromProperties(final Properties properties) {
            {
                @Nullable final String enable = properties.getProperty(FieldDefinitions.ENABLE.getPropertyKey());
                if (enable != null) {
                    setEnable(true);
                }
            }

            return this;
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinitionWithDefault<Boolean> ENABLE = new ConfigurationFieldDefinitionWithDefault<>(Boolean.FALSE, "twks.enableGeoSPARQL");
        public final static ConfigurationFieldDefinition GEOMETRY_INDEX_SIZE = new ConfigurationFieldDefinition("twks.GeoSPARQLGeometryIndexSize");
    }
}
