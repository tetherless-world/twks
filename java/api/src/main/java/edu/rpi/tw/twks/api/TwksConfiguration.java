package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwksConfiguration extends AbstractConfiguration {
    private final Path dumpDirectoryPath;
    private final TwksGeoSPARQLConfiguration geoSparqlConfiguration;
    private final TwksGraphNameCacheConfiguration graphNameCacheConfiguration;

    protected TwksConfiguration(final Builder<?, ?> builder) {
        this.dumpDirectoryPath = builder.getDumpDirectoryPath();
        this.geoSparqlConfiguration = builder.getGeoSparqlConfiguration();
        this.graphNameCacheConfiguration = builder.getGraphNameCacheConfiguration();
    }

    public final Path getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    public final TwksGeoSPARQLConfiguration getGeoSparqlConfiguration() {
        return geoSparqlConfiguration;
    }

    public final TwksGraphNameCacheConfiguration getGraphNameCacheConfiguration() {
        return graphNameCacheConfiguration;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("dumpDirectoryPath", dumpDirectoryPath)
                .add("geoSparqlConfiguration", geoSparqlConfiguration.getEnable() ? geoSparqlConfiguration : null)
                .add("graphNameCacheConfiguration", graphNameCacheConfiguration.getEnable() ? graphNameCacheConfiguration : null);
    }

    public abstract static class Builder<BuilderT extends Builder<?, ?>, TwksConfigurationT extends TwksConfiguration> extends AbstractConfiguration.Builder<BuilderT, TwksConfigurationT> {
        private Path dumpDirectoryPath = PropertyDefinitions.DUMP_DIRECTORY_PATH.getDefault();
        private TwksGeoSPARQLConfiguration geoSparqlConfiguration = TwksGeoSPARQLConfiguration.builder().setEnable(false).build();
        private TwksGraphNameCacheConfiguration graphNameCacheConfiguration = TwksGraphNameCacheConfiguration.builder().setEnable(false).build();

        @Override
        public abstract TwksConfigurationT build();

        public final Path getDumpDirectoryPath() {
            return dumpDirectoryPath;
        }

        @SuppressWarnings("unchecked")
        public final BuilderT setDumpDirectoryPath(final Path dumpDirectoryPath) {
            this.dumpDirectoryPath = dumpDirectoryPath;
            markDirty();
            return (BuilderT) this;
        }

        public final TwksGeoSPARQLConfiguration getGeoSparqlConfiguration() {
            return geoSparqlConfiguration;
        }

        @SuppressWarnings("unchecked")
        public final BuilderT setGeoSparqlConfiguration(final TwksGeoSPARQLConfiguration geoSparqlConfiguration) {
            this.geoSparqlConfiguration = geoSparqlConfiguration;
            markDirty();
            return (BuilderT) this;
        }

        public final TwksGraphNameCacheConfiguration getGraphNameCacheConfiguration() {
            return graphNameCacheConfiguration;
        }

        @SuppressWarnings("unchecked")
        public final BuilderT setGraphNameCacheConfiguration(final TwksGraphNameCacheConfiguration graphNameCacheConfiguration) {
            this.graphNameCacheConfiguration = checkNotNull(graphNameCacheConfiguration);
            markDirty();
            return (BuilderT) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public BuilderT set(final ConfigurationWrapper properties) {
            {
                final TwksGeoSPARQLConfiguration.Builder geoSparqlConfigurationBuilder = TwksGeoSPARQLConfiguration.builder().set(properties);
                if (geoSparqlConfigurationBuilder.isDirty()) {
                    setGeoSparqlConfiguration(geoSparqlConfigurationBuilder.build());
                }
            }

            {
                final TwksGraphNameCacheConfiguration.Builder graphNameCacheConfigurationBuilder = TwksGraphNameCacheConfiguration.builder().set(properties);
                if (graphNameCacheConfigurationBuilder.isDirty()) {
                    setGraphNameCacheConfiguration(graphNameCacheConfigurationBuilder.build());
                }
            }

            properties.getPath(PropertyDefinitions.DUMP_DIRECTORY_PATH).ifPresent(value -> setDumpDirectoryPath(value));

            return (BuilderT) this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinitionWithDefault<Path> DUMP_DIRECTORY_PATH = new PropertyDefinitionWithDefault<>(Paths.get("/dump"), "dump");
    }
}
