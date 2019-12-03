package edu.rpi.tw.twks.servlet;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.AbstractConfiguration;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwksServletConfiguration extends AbstractConfiguration {
    private final Optional<Path> extcpDirectoryPath;
    private final Path extfsDirectoryPath;
    private final TwksFactoryConfiguration factoryConfiguration;
    private final String serverBaseUrl;

    private TwksServletConfiguration(final Builder builder) {
        this.extcpDirectoryPath = builder.getExtcpDirectoryPath();
        this.extfsDirectoryPath = builder.getExtfsDirectoryPath();
        this.factoryConfiguration = builder.getFactoryConfiguration();
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

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("extcpDirectoryPath", getExtcpDirectoryPath().orElse(null)).add("extfsDirectoryPath", getExtfsDirectoryPath()).add("serverBaseUrl", getServerBaseUrl());
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, TwksServletConfiguration> {
        private Optional<Path> extcpDirectoryPath = Optional.empty();
        private Path extfsDirectoryPath = PropertyDefinitions.EXTFS_DIRECTORY_PATH.getDefault();
        private TwksFactoryConfiguration factoryConfiguration = TwksFactoryConfiguration.builder().build();
        private String serverBaseUrl = PropertyDefinitions.SERVER_BASE_URL.getDefault();

        private Builder() {
        }

        @Override
        public final TwksServletConfiguration build() {
            return new TwksServletConfiguration(this);
        }

        public final Optional<Path> getExtcpDirectoryPath() {
            return extcpDirectoryPath;
        }

        public final Builder setExtcpDirectoryPath(final Optional<Path> extcpDirectoryPath) {
            this.extcpDirectoryPath = checkNotNull(extcpDirectoryPath);
            return this;
        }

        public final Path getExtfsDirectoryPath() {
            return extfsDirectoryPath;
        }

        public final Builder setExtfsDirectoryPath(final Path extfsDirectoryPath) {
            this.extfsDirectoryPath = checkNotNull(extfsDirectoryPath);
            return this;
        }

        public final TwksFactoryConfiguration getFactoryConfiguration() {
            return factoryConfiguration;
        }

        public final Builder setFactoryConfiguration(final TwksFactoryConfiguration factoryConfiguration) {
            this.factoryConfiguration = checkNotNull(factoryConfiguration);
            return this;
        }

        public final String getServerBaseUrl() {
            return serverBaseUrl;
        }

        public final Builder setServerBaseUrl(final String serverBaseUrl) {
            this.serverBaseUrl = checkNotNull(serverBaseUrl);
            return this;
        }

        @Override
        public final Builder setFromProperties(final PropertiesWrapper properties) {
            {
                final TwksFactoryConfiguration factoryConfiguration = TwksFactoryConfiguration.builder().setFromProperties(properties).build();
                if (!factoryConfiguration.isEmpty()) {
                    this.factoryConfiguration = checkNotNull(factoryConfiguration);
                }
            }

            {
                final Optional<Path> value = properties.getPath(PropertyDefinitions.EXTCP_DIRECTORY_PATH);
                if (value.isPresent()) {
                    setExtcpDirectoryPath(value);
                }
            }

            {
                final Optional<Path> value = properties.getPath(PropertyDefinitions.EXTFS_DIRECTORY_PATH);
                if (value.isPresent()) {
                    setExtfsDirectoryPath(value.get());
                }
            }

            setServerBaseUrl(properties.getString(PropertyDefinitions.SERVER_BASE_URL).orElse(serverBaseUrl));

            return this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinition EXTCP_DIRECTORY_PATH = new PropertyDefinition("extcp");
        public final static PropertyDefinitionWithDefault<Path> EXTFS_DIRECTORY_PATH = new PropertyDefinitionWithDefault<>(Paths.get("/extfs"), "extfs");
        public final static PropertyDefinitionWithDefault<String> SERVER_BASE_URL = new PropertyDefinitionWithDefault<>("http://localhost:8080", "serverBaseUrl");
    }
}
