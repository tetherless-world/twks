package edu.rpi.tw.twks.agraph;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Optional;

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
        public final Builder setFromProperties(final PropertiesWrapper properties) {
            setCatalogId(properties.getString(PropertyDefinitions.CATALOG_ID).orElse(catalogId));
            setPassword(properties.getString(PropertyDefinitions.PASSWORD).orElse(password));
            {
                final Optional<String> value = properties.getString(PropertyDefinitions.SERVER_URL);
                if (value.isPresent()) {
                    setServerUrl(value.get());
                }
            }
            setUsername(properties.getString(PropertyDefinitions.USERNAME).orElse(username));
            return super.setFromProperties(properties);
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
