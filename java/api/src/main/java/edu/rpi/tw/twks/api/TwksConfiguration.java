package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwksConfiguration extends AbstractConfiguration {
    private final Path dumpDirectoryPath;
    private final TwksGraphNameCacheConfiguration graphNameCacheConfiguration;

    protected TwksConfiguration(final Builder builder) {
        this.dumpDirectoryPath = builder.getDumpDirectoryPath();
        this.graphNameCacheConfiguration = builder.getGraphNameCacheConfiguration();
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
                .add("dumpDirectoryPath", dumpDirectoryPath).add("graphNameCacheConfiguration", graphNameCacheConfiguration.getEnable() ? graphNameCacheConfiguration : null);
    }

    public abstract static class Builder<BuilderT extends Builder<?, ?>, TwksConfigurationT extends TwksConfiguration> extends AbstractConfiguration.Builder<BuilderT, TwksConfigurationT> {
        private Path dumpDirectoryPath = PropertyDefinitions.DUMP_DIRECTORY_PATH.getDefault();
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
        public BuilderT setFromProperties(final PropertiesWrapper properties) {
            {
                final TwksGraphNameCacheConfiguration graphNameCacheConfiguration = TwksGraphNameCacheConfiguration.builder().setFromProperties(properties).build();
                if (graphNameCacheConfiguration.getEnable()) {
                    setGraphNameCacheConfiguration(graphNameCacheConfiguration);
                }
            }

            {
                final Optional<Path> dumpDirectoryPath = properties.getPath(PropertyDefinitions.DUMP_DIRECTORY_PATH);
                if (dumpDirectoryPath.isPresent()) {
                    setDumpDirectoryPath(dumpDirectoryPath.get());
                }
            }

            return (BuilderT) this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinitionWithDefault<Path> DUMP_DIRECTORY_PATH = new PropertyDefinitionWithDefault<>(Paths.get("/dump"), "dump");
    }
}
