package edu.rpi.tw.twks.server;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.factory.TwksConfiguration;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServletConfiguration extends TwksConfiguration {
    public final static Path EXTFS_DIRECTORY_PATH_DEFAULT = Paths.get("/extfs");
    public final static String SERVER_BASE_URL_DEFAULT = "http://localhost:8080";

    private Optional<Path> extcpDirectoryPath = Optional.empty();
    private Path extfsDirectoryPath = EXTFS_DIRECTORY_PATH_DEFAULT;
    private String serverBaseUrl = SERVER_BASE_URL_DEFAULT;

    public final Path getExtfsDirectoryPath() {
        return extfsDirectoryPath;
    }

    public final ServletConfiguration setExtfsDirectoryPath(final Path extfsDirectoryPath) {
        this.extfsDirectoryPath = checkNotNull(extfsDirectoryPath);
        return this;
    }

    public final Optional<Path> getExtcpDirectoryPath() {
        return extcpDirectoryPath;
    }

    public final ServletConfiguration setExtcpDirectoryPath(final Optional<Path> extcpDirectoryPath) {
        this.extcpDirectoryPath = checkNotNull(extcpDirectoryPath);
        return this;
    }

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public ServletConfiguration setServerBaseUrl(final String serverBaseUrl) {
        this.serverBaseUrl = checkNotNull(serverBaseUrl);
        return this;
    }

    @Override
    public TwksConfiguration setFromProperties(final Properties properties) {
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

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("extcpDirectoryPath", getExtcpDirectoryPath().orElse(null)).add("extfsDirectoryPath", getExtfsDirectoryPath()).add("serverBaseUrl", getServerBaseUrl());
    }

    public final static class PropertyKeys extends TwksConfiguration.PropertyKeys {
        public final static String EXTCP_DIRECTORY_PATH = "twks.extcp";
        public final static String EXTFS_DIRECTORY_PATH = "twks.extfs";
        public final static String SERVER_BASE_URL = "twks.serverBaseUrl";
    }
}
