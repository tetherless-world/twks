package edu.rpi.tw.twks.agraph;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.nio.file.Path;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AllegroGraphTwksConfiguration extends TwksConfiguration {
    private final String password;
    private final String serverUrl;
    private final String username;

    private AllegroGraphTwksConfiguration(final Path dumpDirectoryPath, final String password, final String serverUrl, final String username) {
        super(dumpDirectoryPath);
        this.password = checkNotNull(password);
        this.serverUrl = checkNotNull(serverUrl);
        this.username = checkNotNull(username);
    }

    public final static Builder builder() {
        return new Builder();
    }

    @Override
    protected final MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("serverUrl", serverUrl).add("username", username);
    }

    public final static class Builder extends TwksConfiguration.Builder<Builder, AllegroGraphTwksConfiguration> {
        private String password = FieldDefinitions.PASSWORD.getDefault();
        private String serverUrl = null;
        private String username = FieldDefinitions.USERNAME.getDefault();

        @Override
        public final AllegroGraphTwksConfiguration build() {
            checkNotNull(serverUrl, "must set server URL");
            return new AllegroGraphTwksConfiguration(getDumpDirectoryPath(), password, serverUrl, username);
        }

        public final String getPassword() {
            return password;
        }

        public final Builder setPassword(final String password) {
            this.password = checkNotNull(password);
            return this;
        }

        public final @Nullable
        String getServerUrl() {
            return serverUrl;
        }

        public final Builder setServerUrl(final String serverUrl) {
            this.serverUrl = checkNotNull(serverUrl);
            return this;
        }

        public final String getUsername() {
            return username;
        }

        public final Builder setUsername(final String username) {
            this.username = checkNotNull(username);
            return this;
        }

        public final boolean isValid() {
            return serverUrl != null;
        }

        @Override
        public final Builder setFromProperties(final Properties properties) {
            setPassword(properties.getProperty(FieldDefinitions.PASSWORD.getPropertyKey(), password));
            {
                @Nullable final String value = properties.getProperty(FieldDefinitions.SERVER_URL.getPropertyKey());
                if (value != null) {
                    setServerUrl(value);
                }
            }
            setUsername(properties.getProperty(FieldDefinitions.USERNAME.getPropertyKey(), username));
            return super.setFromProperties(properties);
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinitionWithDefault<String> PASSWORD = new ConfigurationFieldDefinitionWithDefault<>("xyzzy", "twks.agraphPassword");
        public final static ConfigurationFieldDefinition SERVER_URL = new ConfigurationFieldDefinition("twks.agraphServerUrl");
        public final static ConfigurationFieldDefinitionWithDefault<String> USERNAME = new ConfigurationFieldDefinitionWithDefault<>("test", "twks.agraphUsername");
    }
}
