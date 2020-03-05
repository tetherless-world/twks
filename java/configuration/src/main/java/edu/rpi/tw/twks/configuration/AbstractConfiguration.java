package edu.rpi.tw.twks.configuration;

import com.google.common.base.MoreObjects;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractConfiguration {
    private final static String PROPERTY_KEY_PREFIX = "twks";

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
        private boolean dirty = false;

        public abstract ConfigurationT build();

        /**
         * Has a setter been called?
         *
         * @return true if a setter has been called
         */
        public final boolean isDirty() {
            return dirty;
        }

        /**
         * Mark that a setter has been called.
         */
        protected final void markDirty() {
            dirty = true;
        }

        public final BuilderT set(final Configuration configuration) {
            return set(new ConfigurationWrapper(configuration));
        }

        public abstract BuilderT set(final ConfigurationWrapper properties);

        @SuppressWarnings("unchecked")
        public final BuilderT setFromEnvironment() {
            set(new EnvironmentConfiguration().subset(PROPERTY_KEY_PREFIX));
            set(new SystemConfiguration().subset(PROPERTY_KEY_PREFIX));
            return (BuilderT) this;
        }

        protected final static class ConfigurationWrapper {
            private final Configuration delegate;

            private ConfigurationWrapper(final Configuration delegate) {
                this.delegate = checkNotNull(delegate);
            }

            public final Optional<Boolean> getBoolean(final PropertyDefinition definition) {
                @Nullable final String value = getProperty(definition);
                return value != null ? Optional.of(Boolean.TRUE) : Optional.empty();
            }

            public final Optional<Integer> getInteger(final PropertyDefinition definition) {
                @Nullable final String value = getProperty(definition);
                return value != null ? Optional.of(Integer.parseInt(value)) : Optional.empty();
            }

            public final Optional<Path> getPath(final PropertyDefinition definition) {
                @Nullable final String value = getProperty(definition);
                return value != null ? Optional.of(Paths.get(value)) : Optional.empty();
            }

            private @Nullable
            String getProperty(final PropertyDefinition definition) {
                @Nullable final Object value = delegate.getProperty(definition.getKey());
                return value instanceof String ? (String) value : null;
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
