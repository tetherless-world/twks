package edu.rpi.tw.twks.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.configuration.AbstractConfiguration;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwksServerConfiguration extends AbstractConfiguration {
    private final Optional<Path> extcpDirectoryPath;
    private final Path extfsDirectoryPath;
    private final TwksFactoryConfiguration factoryConfiguration;
    private final Optional<ImmutableList<Path>> initialNanopublicationFilePaths;
    private final Optional<Path> initialNanopublicationsDirectoryPath;
    private final String serverBaseUrl;

    private TwksServerConfiguration(final Builder builder) {
        this.extcpDirectoryPath = builder.getExtcpDirectoryPath();
        this.extfsDirectoryPath = builder.getExtfsDirectoryPath();
        this.factoryConfiguration = builder.getFactoryConfiguration();
        this.initialNanopublicationsDirectoryPath = builder.getInitialNanopublicationsDirectoryPath();
        this.initialNanopublicationFilePaths = builder.getInitialNanopublicationFilePaths();
        this.serverBaseUrl = builder.getServerBaseUrl();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final Optional<Path> getExtcpDirectoryPath() {
        return extcpDirectoryPath;
    }

    public final Path getExtfsDirectoryPath() {
        return extfsDirectoryPath;
    }

    public final TwksFactoryConfiguration getFactoryConfiguration() {
        return factoryConfiguration;
    }

    public final Optional<ImmutableList<Path>> getInitialNanopublicationFilePaths() {
        return initialNanopublicationFilePaths;
    }

    public final Optional<Path> getInitialNanopublicationsDirectoryPath() {
        return initialNanopublicationsDirectoryPath;
    }

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("extcpDirectoryPath", getExtcpDirectoryPath().orElse(null))
                .add("extfsDirectoryPath", getExtfsDirectoryPath())
                .add("factoryConfiguration", getFactoryConfiguration())
                .add("initialNanopublicationDirectoryPath", getInitialNanopublicationsDirectoryPath().orElse(null))
                .add("initialNanopublicationFilePaths", getInitialNanopublicationFilePaths().orElse(null))
                .add("serverBaseUrl", getServerBaseUrl());
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, TwksServerConfiguration> {
        private Optional<Path> extcpDirectoryPath = Optional.empty();
        private Path extfsDirectoryPath = PropertyDefinitions.EXTFS_DIRECTORY_PATH.getDefault();
        private TwksFactoryConfiguration factoryConfiguration = TwksFactoryConfiguration.builder().build();
        private Optional<ImmutableList<Path>> initialNanopublicationFilePaths = Optional.empty();
        private Optional<Path> initialNanopublicationsDirectoryPath = Optional.empty();
        private String serverBaseUrl = PropertyDefinitions.SERVER_BASE_URL.getDefault();

        private Builder() {
        }

        @Override
        public final TwksServerConfiguration build() {
            return new TwksServerConfiguration(this);
        }

        public final Optional<Path> getExtcpDirectoryPath() {
            return extcpDirectoryPath;
        }

        public final Builder setExtcpDirectoryPath(final Optional<Path> extcpDirectoryPath) {
            this.extcpDirectoryPath = checkNotNull(extcpDirectoryPath);
            markDirty();
            return this;
        }

        public final Path getExtfsDirectoryPath() {
            return extfsDirectoryPath;
        }

        public final Builder setExtfsDirectoryPath(final Path extfsDirectoryPath) {
            this.extfsDirectoryPath = checkNotNull(extfsDirectoryPath);
            markDirty();
            return this;
        }

        public final TwksFactoryConfiguration getFactoryConfiguration() {
            return factoryConfiguration;
        }

        public final Builder setFactoryConfiguration(final TwksFactoryConfiguration factoryConfiguration) {
            this.factoryConfiguration = checkNotNull(factoryConfiguration);
            markDirty();
            return this;
        }

        public final Optional<ImmutableList<Path>> getInitialNanopublicationFilePaths() {
            return initialNanopublicationFilePaths;
        }

        public final Builder setInitialNanopublicationFilePaths(final ImmutableList<Path> value) {
            this.initialNanopublicationFilePaths = Optional.of(value);
            markDirty();
            return this;
        }

        public final Optional<Path> getInitialNanopublicationsDirectoryPath() {
            return initialNanopublicationsDirectoryPath;
        }

        public final Builder setInitialNanopublicationsDirectoryPath(final Path value) {
            this.initialNanopublicationsDirectoryPath = Optional.of(value);
            markDirty();
            return this;
        }

        public final String getServerBaseUrl() {
            return serverBaseUrl;
        }

        public final Builder setServerBaseUrl(final String serverBaseUrl) {
            this.serverBaseUrl = checkNotNull(serverBaseUrl);
            markDirty();
            return this;
        }

        @Override
        public final Builder set(final ConfigurationWrapper properties) {
            {
                final TwksFactoryConfiguration.Builder factoryConfigurationBuilder = TwksFactoryConfiguration.builder().set(properties);
                if (factoryConfigurationBuilder.isDirty()) {
                    setFactoryConfiguration(factoryConfigurationBuilder.build());
                }
            }

            properties.getPath(PropertyDefinitions.EXTCP_DIRECTORY_PATH).ifPresent(value -> setExtcpDirectoryPath(Optional.of(value)));
            properties.getPath(PropertyDefinitions.EXTFS_DIRECTORY_PATH).ifPresent(value -> setExtfsDirectoryPath(value));
            properties.getPath(PropertyDefinitions.INITIAL_NANOPUBLICATIONS_DIRECTORY_PATH).ifPresent(value -> setInitialNanopublicationsDirectoryPath(value));
            properties.getPaths(PropertyDefinitions.INITIAL_NANOPUBLICATION_FILE_PATH).ifPresent(value -> setInitialNanopublicationFilePaths(value));
            properties.getString(PropertyDefinitions.SERVER_BASE_URL).ifPresent(value -> setServerBaseUrl(value));

            return this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinition EXTCP_DIRECTORY_PATH = new PropertyDefinition("extcp");
        public final static PropertyDefinitionWithDefault<Path> EXTFS_DIRECTORY_PATH = new PropertyDefinitionWithDefault<>(Paths.get("/extfs"), "extfs");
        public final static PropertyDefinition INITIAL_NANOPUBLICATIONS_DIRECTORY_PATH = new PropertyDefinition("initialNanopublicationsDirectory");
        public final static PropertyDefinition INITIAL_NANOPUBLICATION_FILE_PATH = new PropertyDefinition("initialNanopublicationFilePath");
        public final static PropertyDefinitionWithDefault<String> SERVER_BASE_URL = new PropertyDefinitionWithDefault<>("http://localhost:8080", "serverBaseUrl");
    }
}
