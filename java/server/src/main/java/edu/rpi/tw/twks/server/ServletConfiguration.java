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

public final class ServletConfiguration extends AbstractConfiguration<ServletConfiguration> {
    public final static Path EXTFS_DIRECTORY_PATH_DEFAULT = Paths.get("/extfs");
    public final static String SERVER_BASE_URL_DEFAULT = "http://localhost:8080";
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
        private Path extfsDirectoryPath = EXTFS_DIRECTORY_PATH_DEFAULT;
        private TwksFactoryConfiguration factoryConfiguration = TwksFactoryConfiguration.builder().build();
        private String serverBaseUrl = SERVER_BASE_URL_DEFAULT;

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
                @Nullable final String value = properties.getProperty(PropertyKeys.EXTCP_DIRECTORY_PATH);
                if (value != null) {
                    setExtcpDirectoryPath(Optional.of(Paths.get(value)));
                }
            }

            {
                @Nullable final String value = properties.getProperty(PropertyKeys.EXTFS_DIRECTORY_PATH);
                if (value != null) {
                    setExtfsDirectoryPath(Paths.get(value));
                }
            }

            setServerBaseUrl(properties.getProperty(PropertyKeys.SERVER_BASE_URL, serverBaseUrl));

            return this;
        }

        @Override
        public Builder setFromSystemProperties() {
            return (Builder) super.setFromSystemProperties();
        }
    }

    public final static class PropertyKeys {
        public final static String EXTCP_DIRECTORY_PATH = "twks.extcp";
        public final static String EXTFS_DIRECTORY_PATH = "twks.extfs";
        public final static String SERVER_BASE_URL = "twks.serverBaseUrl";
    }
}
