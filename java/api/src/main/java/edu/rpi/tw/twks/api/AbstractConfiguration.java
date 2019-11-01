package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import java.util.Properties;

public abstract class AbstractConfiguration<ConfigurationT extends AbstractConfiguration<?>> {
    protected AbstractConfiguration() {
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this).omitNullValues();
    }

    public abstract static class Builder<BuilderT extends Builder, ConfigurationT extends AbstractConfiguration<?>> {
        public abstract ConfigurationT build();

        public abstract BuilderT setFromProperties(final Properties properties);

        public BuilderT setFromSystemProperties() {
            return setFromProperties(System.getProperties());
        }
    }
}
