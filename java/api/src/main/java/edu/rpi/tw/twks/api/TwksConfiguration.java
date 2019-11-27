package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwksConfiguration extends AbstractConfiguration {
    private final Path dumpDirectoryPath;
    private final TwksGraphNameCacheConfiguration graphNameCacheConfiguration;

    protected TwksConfiguration(final Path dumpDirectoryPath, final TwksGraphNameCacheConfiguration graphNameCacheConfiguration) {
        this.dumpDirectoryPath = checkNotNull(dumpDirectoryPath);
        this.graphNameCacheConfiguration = checkNotNull(graphNameCacheConfiguration);
    }

    public final Path getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    public final TwksGraphNameCacheConfiguration getGraphNameCacheConfiguration() {
        return graphNameCacheConfiguration;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("dumpDirectoryPath", dumpDirectoryPath);
    }

    public abstract static class Builder<BuilderT extends Builder<?, ?>, TwksConfigurationT extends TwksConfiguration> extends AbstractConfiguration.Builder<BuilderT, TwksConfigurationT> {
        private Path dumpDirectoryPath = FieldDefinitions.DUMP_DIRECTORY_PATH.getDefault();
        private TwksGraphNameCacheConfiguration graphNameCacheConfiguration = TwksGraphNameCacheConfiguration.builder().setEnable(false).build();

        @Override
        public abstract TwksConfigurationT build();

        public final Path getDumpDirectoryPath() {
            return dumpDirectoryPath;
        }

        @SuppressWarnings("unchecked")
        public final BuilderT setDumpDirectoryPath(final Path dumpDirectoryPath) {
            this.dumpDirectoryPath = dumpDirectoryPath;
            return (BuilderT) this;
        }

        public final TwksGraphNameCacheConfiguration getGraphNameCacheConfiguration() {
            return graphNameCacheConfiguration;
        }

        @SuppressWarnings("unchecked")
        public final BuilderT setGraphNameCacheConfiguration(final TwksGraphNameCacheConfiguration graphNameCacheConfiguration) {
            this.graphNameCacheConfiguration = checkNotNull(graphNameCacheConfiguration);
            return (BuilderT) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public BuilderT setFromProperties(final Properties properties) {
            {
                final TwksGraphNameCacheConfiguration graphNameCacheConfiguration = TwksGraphNameCacheConfiguration.builder().setFromProperties(properties).build();
                if (graphNameCacheConfiguration.getEnable()) {
                    setGraphNameCacheConfiguration(graphNameCacheConfiguration);
                }
            }

            {
                @Nullable final String dumpDirectoryPath = properties.getProperty(FieldDefinitions.DUMP_DIRECTORY_PATH.getPropertyKey());
                if (dumpDirectoryPath != null) {
                    setDumpDirectoryPath(Paths.get(dumpDirectoryPath));
                }
            }

            return (BuilderT) this;
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinitionWithDefault<Boolean> CACHE_GRAPH_NAMES = new ConfigurationFieldDefinitionWithDefault<>(Boolean.FALSE, "twks.cacheGraphNames");
        public final static ConfigurationFieldDefinitionWithDefault<Path> DUMP_DIRECTORY_PATH = new ConfigurationFieldDefinitionWithDefault<>(Paths.get("/dump"), "twks.dump");
    }
}
