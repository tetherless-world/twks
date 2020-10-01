package edu.rpi.tw.twks.configuration;

import com.google.common.base.MoreObjects;

public final class GeoSPARQLConfiguration extends AbstractConfiguration {
    private final boolean enable;

    private GeoSPARQLConfiguration(final Builder builder) {
        this.enable = builder.getEnable();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final boolean getEnable() {
        return enable;
    }

    @Override
    protected final MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("enable", enable);
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, GeoSPARQLConfiguration> {
        private boolean enable = PropertyDefinitions.ENABLE.getDefault();

        @Override
        public GeoSPARQLConfiguration build() {
            return new GeoSPARQLConfiguration(this);
        }

        public final boolean getEnable() {
            return enable;
        }

        public final Builder setEnable(final boolean enable) {
            this.enable = enable;
            markDirty();
            return this;
        }

        @Override
        public final Builder set(final ConfigurationWrapper properties) {
            properties.getBoolean(PropertyDefinitions.ENABLE).ifPresent(value -> setEnable(value));
            return this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinitionWithDefault<Boolean> ENABLE = new PropertyDefinitionWithDefault<>(Boolean.FALSE, "enableGeoSPARQL");
//        public final static PropertyDefinition GEOMETRY_INDEX_SIZE = new PropertyDefinition("GeoSPARQLGeometryIndexSize");
    }
}
