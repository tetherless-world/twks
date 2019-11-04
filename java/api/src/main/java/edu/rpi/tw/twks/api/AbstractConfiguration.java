package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractConfiguration {
    protected AbstractConfiguration() {
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this).omitNullValues();
    }

    public abstract static class Builder<BuilderT extends Builder, ConfigurationT extends AbstractConfiguration> {
        public abstract ConfigurationT build();

        public abstract BuilderT setFromProperties(final Properties properties);

        public BuilderT setFromSystemProperties() {
            return setFromProperties(System.getProperties());
        }
    }

    protected static class ConfigurationFieldDefinition {
        private final String propertyKey;

        public ConfigurationFieldDefinition(final String propertyKey) {
            this.propertyKey = checkNotNull(propertyKey);
        }

        public final String getPropertyKey() {
            return propertyKey;
        }
    }

    protected final static class ConfigurationFieldDefinitionWithDefault<T> extends ConfigurationFieldDefinition {
        private final T default_;

        public ConfigurationFieldDefinitionWithDefault(final T default_, final String propertyKey) {
            super(propertyKey);
            this.default_ = checkNotNull(default_);
        }

        public final T getDefault() {
            return default_;
        }
    }
}
