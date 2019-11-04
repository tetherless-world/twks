package edu.rpi.tw.twks.agraph;

import edu.rpi.tw.twks.api.TwksConfiguration;

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

    public final static class Builder extends TwksConfiguration.Builder {
        private String password = FieldDefinitions.PASSWORD.getDefault();
        private String serverUrl = FieldDefinitions.SERVER_URL.getDefault();
        private String username = FieldDefinitions.USERNAME.getDefault();

        @Override
        public final AllegroGraphTwksConfiguration build() {
            return new AllegroGraphTwksConfiguration(getDumpDirectoryPath(), password, serverUrl, username);
        }

        public final String getPassword() {
            return password;
        }

        public final Builder setPassword(final String password) {
            this.password = checkNotNull(password);
            return this;
        }

        public final String getServerUrl() {
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

        @Override
        public final Builder setFromProperties(final Properties properties) {
            setPassword(properties.getProperty(FieldDefinitions.PASSWORD.getPropertyKey(), password));
            setServerUrl(properties.getProperty(FieldDefinitions.SERVER_URL.getPropertyKey(), serverUrl));
            setUsername(properties.getProperty(FieldDefinitions.USERNAME.getPropertyKey(), username));
            return (Builder) super.setFromProperties(properties);
        }

        @Override
        public final Builder setFromSystemProperties() {
            return (Builder) super.setFromSystemProperties();
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinitionWithDefault<String> PASSWORD = new ConfigurationFieldDefinitionWithDefault<>("xyzzy", "twks.agraphPassword");
        public final static ConfigurationFieldDefinitionWithDefault<String> SERVER_URL = new ConfigurationFieldDefinitionWithDefault<>("http://localhost:10035", "twks.agraphServerUrl");
        public final static ConfigurationFieldDefinitionWithDefault<String> USERNAME = new ConfigurationFieldDefinitionWithDefault<>("test", "twks.agraphUsername");
    }
}
