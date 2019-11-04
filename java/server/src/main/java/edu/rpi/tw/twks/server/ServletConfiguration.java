package edu.rpi.tw.twks.server;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.AbstractConfiguration;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServletConfiguration extends AbstractConfiguration {
    private final Optional<Path> extcpDirectoryPath;
    private final Path extfsDirectoryPath;
    private final TwksFactoryConfiguration factoryConfiguration;
    private final String serverBaseUrl;

    private ServletConfiguration(final Optional<Path> extcpDirectoryPath, final Path extfsDirectoryPath, final TwksFactoryConfiguration factoryConfiguration, final String serverBaseUrl) {
        this.extcpDirectoryPath = checkNotNull(extcpDirectoryPath);
        this.extfsDirectoryPath = checkNotNull(extfsDirectoryPath);
        this.factoryConfiguration = checkNotNull(factoryConfiguration);
        this.serverBaseUrl = checkNotNull(serverBaseUrl);
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

    public final static class Builder extends AbstractConfiguration.Builder<Builder, ServletConfiguration> {
        private Optional<Path> extcpDirectoryPath = Optional.empty();
        private Path extfsDirectoryPath = FieldDefinitions.EXTFS_DIRECTORY_PATH.getDefault();
        private TwksFactoryConfiguration factoryConfiguration = TwksFactoryConfiguration.builder().build();
        private String serverBaseUrl = FieldDefinitions.SERVER_BASE_URL.getDefault();

        private Builder() {
        }

        @Override
        public final ServletConfiguration build() {
            return new ServletConfiguration(extcpDirectoryPath, extfsDirectoryPath, factoryConfiguration, serverBaseUrl);
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

        public Builder setServerBaseUrl(final String serverBaseUrl) {
            this.serverBaseUrl = checkNotNull(serverBaseUrl);
            return this;
        }

        @Override
        public Builder setFromProperties(final Properties properties) {
            {
                final TwksFactoryConfiguration factoryConfiguration = TwksFactoryConfiguration.builder().setFromProperties(properties).build();
                if (!factoryConfiguration.isEmpty()) {
                    this.factoryConfiguration = checkNotNull(factoryConfiguration);
                }
            }

            {
                @Nullable final String value = properties.getProperty(FieldDefinitions.EXTCP_DIRECTORY_PATH.getPropertyKey());
                if (value != null) {
                    setExtcpDirectoryPath(Optional.of(Paths.get(value)));
                }
            }

            {
                @Nullable final String value = properties.getProperty(FieldDefinitions.EXTFS_DIRECTORY_PATH.getPropertyKey());
                if (value != null) {
                    setExtfsDirectoryPath(Paths.get(value));
                }
            }

            setServerBaseUrl(properties.getProperty(FieldDefinitions.SERVER_BASE_URL.getPropertyKey(), serverBaseUrl));

            return this;
        }

        @Override
        public Builder setFromSystemProperties() {
            return (Builder) super.setFromSystemProperties();
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinition EXTCP_DIRECTORY_PATH = new ConfigurationFieldDefinition("twks.extcp");
        public final static ConfigurationFieldDefinitionWithDefault<Path> EXTFS_DIRECTORY_PATH = new ConfigurationFieldDefinitionWithDefault<>(Paths.get("/extfs"), "twks.extfs");
        public final static ConfigurationFieldDefinitionWithDefault<String> SERVER_BASE_URL = new ConfigurationFieldDefinitionWithDefault<>("http://localhost:8080", "twks.serverBaseUrl");
    }
}
