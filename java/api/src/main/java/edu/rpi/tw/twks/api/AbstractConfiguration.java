package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
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

    public abstract static class Builder<BuilderT extends Builder<?, ?>, ConfigurationT extends AbstractConfiguration> {
        public abstract ConfigurationT build();

        public final BuilderT setFromProperties(final Properties properties) {
            return setFromProperties(new PropertiesWrapper(properties));
        }

        public abstract BuilderT setFromProperties(final PropertiesWrapper properties);

        public final BuilderT setFromSystemProperties() {
            return setFromProperties(System.getProperties());
        }

        protected final static class PropertiesWrapper {
            private final Properties properties;

            private PropertiesWrapper(final Properties properties) {
                this.properties = checkNotNull(properties);
            }

            public final Optional<Boolean> getBoolean(final PropertyDefinition definition) {
                @Nullable final String value = getProperty(definition);
                return value != null ? Optional.of(Boolean.TRUE) : Optional.empty();
            }

            public final Optional<Path> getPath(final PropertyDefinition definition) {
                @Nullable final String value = getProperty(definition);
                return value != null ? Optional.of(Paths.get(value)) : Optional.empty();
            }

            private @Nullable
            String getProperty(final PropertyDefinition definition) {
                return properties.getProperty("twks." + definition.getKey());
            }

            public final Optional<String> getString(final PropertyDefinition definition) {
                @Nullable final String value = getProperty(definition);
                return Optional.ofNullable(value);
            }
        }
    }

    protected static class PropertyDefinition {
        private final String key;

        public PropertyDefinition(final String key) {
            this.key = checkNotNull(key);
        }

        public final String getKey() {
            return key;
        }
    }

    protected final static class PropertyDefinitionWithDefault<T> extends PropertyDefinition {
        private final T default_;

        public PropertyDefinitionWithDefault(final T default_, final String key) {
            super(key);
            this.default_ = checkNotNull(default_);
        }

        public final T getDefault() {
            return default_;
        }
    }
}
