package edu.rpi.tw.twks.api;

public final class TwksGraphNameCacheConfiguration extends AbstractConfiguration {
    private final boolean enable;

    private TwksGraphNameCacheConfiguration(final Builder builder) {
        this.enable = builder.getEnable();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final boolean getEnable() {
        return enable;
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, TwksGraphNameCacheConfiguration> {
        private boolean enable = PropertyDefinitions.ENABLE.getDefault();

        @Override
        public TwksGraphNameCacheConfiguration build() {
            return new TwksGraphNameCacheConfiguration(this);
        }

        public final boolean getEnable() {
            return enable;
        }

        public final Builder setEnable(final boolean enable) {
            this.enable = enable;
            return this;
        }

        @Override
        public final Builder setFromProperties(final PropertiesWrapper properties) {
            properties.getBoolean(PropertyDefinitions.ENABLE).ifPresent(enable -> setEnable(enable));
            return this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinitionWithDefault<Boolean> ENABLE = new PropertyDefinitionWithDefault<>(Boolean.FALSE, "cacheGraphNames");
    }
}
