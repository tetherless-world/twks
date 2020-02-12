package edu.rpi.tw.twks.agraph;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.configuration.TwksConfiguration;
import edu.umd.cs.findbugs.annotations.Nullable;

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
        private String catalogId = PropertyDefinitions.CATALOG_ID.getDefault();
        private String password = PropertyDefinitions.PASSWORD.getDefault();
        private String repositoryId = PropertyDefinitions.REPOSITORY_ID.getDefault();
        private String serverUrl = null;
        private String username = PropertyDefinitions.USERNAME.getDefault();

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
            markDirty();
            return this;
        }

        public final String getPassword() {
            return password;
        }

        public final Builder setPassword(final String password) {
            this.password = checkNotNull(password);
            markDirty();
            return this;
        }

        public final String getRepositoryId() {
            return repositoryId;
        }

        public final Builder setRepositoryId(final String repositoryId) {
            this.repositoryId = checkNotNull(repositoryId);
            markDirty();
            return this;
        }

        public final @Nullable
        String getServerUrl() {
            return serverUrl;
        }

        public final Builder setServerUrl(final String serverUrl) {
            this.serverUrl = checkNotNull(serverUrl);
            markDirty();
            return this;
        }

        public final String getUsername() {
            return username;
        }

        public final Builder setUsername(final String username) {
            this.username = checkNotNull(username);
            markDirty();
            return this;
        }

        public final boolean isValid() {
            return serverUrl != null;
        }

        @Override
        public final Builder set(final ConfigurationWrapper properties) {
            properties.getString(PropertyDefinitions.CATALOG_ID).ifPresent(value -> setCatalogId(value));
            properties.getString(PropertyDefinitions.PASSWORD).ifPresent(value -> setPassword(value));
            properties.getString(PropertyDefinitions.SERVER_URL).ifPresent(value -> setServerUrl(value));
            properties.getString(PropertyDefinitions.USERNAME).ifPresent(value -> setUsername(value));
            return super.set(properties);
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinitionWithDefault<String> CATALOG_ID = new PropertyDefinitionWithDefault<>("twks-catalog", "agraphCatalogId");
        public final static PropertyDefinitionWithDefault<String> PASSWORD = new PropertyDefinitionWithDefault<>("twks", "agraphPassword");
        public final static PropertyDefinitionWithDefault<String> REPOSITORY_ID = new PropertyDefinitionWithDefault<>("twks-repository", "agraphRepositoryId");
        public final static PropertyDefinition SERVER_URL = new PropertyDefinition("agraphServerUrl");
        public final static PropertyDefinitionWithDefault<String> USERNAME = new PropertyDefinitionWithDefault<>("twks", "agraphUsername");
    }
}
