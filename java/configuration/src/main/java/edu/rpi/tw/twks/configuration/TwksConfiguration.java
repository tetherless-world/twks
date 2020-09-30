package edu.rpi.tw.twks.configuration;

import com.google.common.base.MoreObjects;

import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwksConfiguration extends AbstractConfiguration {
    private final Path dumpDirectoryPath;
    private final TwksGeoSPARQLConfiguration geoSparqlConfiguration;

    protected TwksConfiguration(final Builder<?, ?> builder) {
        this.dumpDirectoryPath = builder.getDumpDirectoryPath();
        this.geoSparqlConfiguration = builder.getGeoSparqlConfiguration();
    }

    public final Path getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    public final TwksGeoSPARQLConfiguration getGeoSparqlConfiguration() {
        return geoSparqlConfiguration;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("dumpDirectoryPath", dumpDirectoryPath)
                .add("geoSparqlConfiguration", geoSparqlConfiguration.getEnable() ? geoSparqlConfiguration : null);
    }

    public abstract static class Builder<BuilderT extends Builder<?, ?>, TwksConfigurationT extends TwksConfiguration> extends AbstractConfiguration.Builder<BuilderT, TwksConfigurationT> {
        private Path dumpDirectoryPath = PropertyDefinitions.DUMP_DIRECTORY_PATH.getDefault();
        private TwksGeoSPARQLConfiguration geoSparqlConfiguration = TwksGeoSPARQLConfiguration.builder().setEnable(false).build();

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

        @Override
        @SuppressWarnings("unchecked")
        public BuilderT set(final ConfigurationWrapper properties) {
            {
                final TwksGeoSPARQLConfiguration.Builder geoSparqlConfigurationBuilder = TwksGeoSPARQLConfiguration.builder().set(properties);
                if (geoSparqlConfigurationBuilder.isDirty()) {
                    setGeoSparqlConfiguration(geoSparqlConfigurationBuilder.build());
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
