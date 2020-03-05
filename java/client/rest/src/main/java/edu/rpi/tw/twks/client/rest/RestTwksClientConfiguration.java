package edu.rpi.tw.twks.client.rest;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import edu.rpi.tw.twks.configuration.AbstractConfiguration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RestTwksClientConfiguration extends AbstractConfiguration {
    private final Optional<Integer> clientConnectionTimeout;
    private final Optional<Integer> clientSoTimeout;
    private final String serverBaseUrl;

    private RestTwksClientConfiguration(final Builder builder) {
        this.clientConnectionTimeout = builder.getClientConnectionTimeout();
        this.clientSoTimeout = builder.getClientSoTimeout();
        this.serverBaseUrl = builder.getServerBaseUrl();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("clientConnectionTimeout", clientConnectionTimeout)
                .add("serverBaseUrl", serverBaseUrl);
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, RestTwksClientConfiguration> {
        private Optional<Integer> clientConnectionTimeout = Optional.absent();
        private Optional<Integer> clientSoTimeout = Optional.absent();
        private String serverBaseUrl = PropertyDefinitions.SERVER_BASE_URL.getDefault();

        @Override
        public final RestTwksClientConfiguration build() {
            return new RestTwksClientConfiguration(this);
        }

        public final Optional<Integer> getClientConnectionTimeout() {
            return clientConnectionTimeout;
        }

        public final Builder setClientConnectionTimeout(final Optional<Integer> clientConnectionTimeout) {
            this.clientConnectionTimeout = checkNotNull(clientConnectionTimeout);
            markDirty();
            return this;
        }

        public final Optional<Integer> getClientSoTimeout() {
            return clientSoTimeout;
        }

        public final Builder setClientSoTimeout(final Optional<Integer> clientSoTimeout) {
            this.clientSoTimeout = clientSoTimeout;
            markDirty();
            return this;
        }

        public final String getServerBaseUrl() {
            return serverBaseUrl;
        }

        public final Builder setServerBaseUrl(final String serverBaseUrl) {
            this.serverBaseUrl = checkNotNull(serverBaseUrl);
            markDirty();
            return this;
        }

        @Override
        public final Builder set(final ConfigurationWrapper properties) {
            properties.getInteger(PropertyDefinitions.CLIENT_CONNECTION_TIMEOUT).ifPresent(value -> setClientConnectionTimeout(Optional.of(value)));
            properties.getInteger(PropertyDefinitions.CLIENT_SO_TIMEOUT).ifPresent(value -> setClientSoTimeout(Optional.of(value)));
            properties.getString(PropertyDefinitions.SERVER_BASE_URL).ifPresent(value -> setServerBaseUrl(value));
            return this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinition CLIENT_CONNECTION_TIMEOUT = new PropertyDefinition("clientConnectionTimeout");
        public final static PropertyDefinition CLIENT_SO_TIMEOUT = new PropertyDefinition("clientSoTimeout");
        public final static PropertyDefinitionWithDefault<String> SERVER_BASE_URL = new PropertyDefinitionWithDefault<>("http://localhost:8080", "serverBaseUrl");
    }
}
