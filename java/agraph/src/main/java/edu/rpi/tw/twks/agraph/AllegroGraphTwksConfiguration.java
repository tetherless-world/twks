package edu.rpi.tw.twks.agraph;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class AllegroGraphTwksConfiguration extends TwksConfiguration {
    private final String catalogId;
    private final String password;
    private final String repositoryId;
    private final String serverUrl;
    private final String username;

    private AllegroGraphTwksConfiguration(final Builder builder) {
        super(builder);
        this.catalogId = builder.getCatalogId();
        this.password = builder.getPassword();
        this.repositoryId = builder.getRepositoryId();
        this.serverUrl = builder.getServerUrl();
        this.username = builder.getUsername();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final String getCatalogId() {
        return catalogId;
    }

    public final String getPassword() {
        return password;
    }

    public final String getRepositoryId() {
        return repositoryId;
    }

    public final String getServerUrl() {
        return serverUrl;
    }

    public final String getUsername() {
        return username;
    }

    @Override
    protected final MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("catalogId", catalogId).add("serverUrl", serverUrl).add("username", username);
    }

    public final static class Builder extends TwksConfiguration.Builder<Builder, AllegroGraphTwksConfiguration> {
        private String catalogId = FieldDefinitions.CATALOG_ID.getDefault();
        private String password = FieldDefinitions.PASSWORD.getDefault();
        private String repositoryId = FieldDefinitions.REPOSITORY_ID.getDefault();
        private String serverUrl = null;
        private String username = FieldDefinitions.USERNAME.getDefault();

        @Override
        public final AllegroGraphTwksConfiguration build() {
            checkNotNull(serverUrl, "must set server URL");
            return new AllegroGraphTwksConfiguration(this);
        }

        public final String getCatalogId() {
            return catalogId;
        }

        public final Builder setCatalogId(final String catalogId) {
            this.catalogId = checkNotNull(catalogId);
            return this;
        }

        public final String getPassword() {
            return password;
        }

        public final Builder setPassword(final String password) {
            this.password = checkNotNull(password);
            return this;
        }

        public final String getRepositoryId() {
            return repositoryId;
        }

        public final Builder setRepositoryId(final String repositoryId) {
            this.repositoryId = checkNotNull(repositoryId);
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
            setCatalogId(properties.getProperty(FieldDefinitions.CATALOG_ID.getPropertyKey(), catalogId));
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
        public final static ConfigurationFieldDefinitionWithDefault<String> CATALOG_ID = new ConfigurationFieldDefinitionWithDefault<>("twks-catalog", "twks.agraphCatalogId");
        public final static ConfigurationFieldDefinitionWithDefault<String> PASSWORD = new ConfigurationFieldDefinitionWithDefault<>("twks", "twks.agraphPassword");
        public final static ConfigurationFieldDefinitionWithDefault<String> REPOSITORY_ID = new ConfigurationFieldDefinitionWithDefault<>("twks-repository", "twks.agraphRepositoryId");
        public final static ConfigurationFieldDefinition SERVER_URL = new ConfigurationFieldDefinition("twks.agraphServerUrl");
        public final static ConfigurationFieldDefinitionWithDefault<String> USERNAME = new ConfigurationFieldDefinitionWithDefault<>("twks", "twks.agraphUsername");
    }
}
