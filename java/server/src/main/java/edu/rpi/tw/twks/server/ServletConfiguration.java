package edu.rpi.tw.twks.server;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServletConfiguration extends TwksFactoryConfiguration {
    public final static Path EXTFS_DIRECTORY_PATH_DEFAULT = Paths.get("/extfs");
    public final static String SERVER_BASE_URL_DEFAULT = "http://localhost:8080";
    private final Optional<Path> extcpDirectoryPath;
    private final Path extfsDirectoryPath;
    private final String serverBaseUrl;

    private ServletConfiguration(final Path dumpDirectoryPath, final Optional<Path> extcpDirectoryPath, final Path extfsDirectoryPath, final String serverBaseUrl, final Optional<String> tdb2Location) {
        super(dumpDirectoryPath, tdb2Location);
        this.extcpDirectoryPath = checkNotNull(extcpDirectoryPath);
        this.extfsDirectoryPath = checkNotNull(extfsDirectoryPath);
        this.serverBaseUrl = checkNotNull(serverBaseUrl);
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final Path getExtfsDirectoryPath() {
        return extfsDirectoryPath;
    }

    public final Optional<Path> getExtcpDirectoryPath() {
        return extcpDirectoryPath;
    }

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("extcpDirectoryPath", getExtcpDirectoryPath().orElse(null)).add("extfsDirectoryPath", getExtfsDirectoryPath()).add("serverBaseUrl", getServerBaseUrl());
    }

    public final static class Builder extends TwksFactoryConfiguration.Builder {
        public final static Path EXTFS_DIRECTORY_PATH_DEFAULT = Paths.get("/extfs");
        public final static String SERVER_BASE_URL_DEFAULT = "http://localhost:8080";
        private Optional<Path> extcpDirectoryPath = Optional.empty();
        private Path extfsDirectoryPath = EXTFS_DIRECTORY_PATH_DEFAULT;
        private String serverBaseUrl = SERVER_BASE_URL_DEFAULT;

        private Builder() {
        }

        @Override
        public final ServletConfiguration build() {
            return new ServletConfiguration(getDumpDirectoryPath(), extcpDirectoryPath, extfsDirectoryPath, serverBaseUrl, getTdb2Location());
        }

        public final Path getExtfsDirectoryPath() {
            return extfsDirectoryPath;
        }

        public final Builder setExtfsDirectoryPath(final Path extfsDirectoryPath) {
            this.extfsDirectoryPath = checkNotNull(extfsDirectoryPath);
            return this;
        }

        public final Optional<Path> getExtcpDirectoryPath() {
            return extcpDirectoryPath;
        }

        public final Builder setExtcpDirectoryPath(final Optional<Path> extcpDirectoryPath) {
            this.extcpDirectoryPath = checkNotNull(extcpDirectoryPath);
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
        public Builder setFromSystemProperties() {
            return (Builder) super.setFromSystemProperties();
        }

        @Override
        public Builder setFromProperties(final Properties properties) {
            super.setFromProperties(properties);

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
    }

    public final static class PropertyKeys extends TwksFactoryConfiguration.PropertyKeys {
        public final static String EXTCP_DIRECTORY_PATH = "twks.extcp";
        public final static String EXTFS_DIRECTORY_PATH = "twks.extfs";
        public final static String SERVER_BASE_URL = "twks.serverBaseUrl";
    }
}
