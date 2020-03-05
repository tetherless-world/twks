package edu.rpi.tw.twks.client.rest;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import edu.rpi.tw.twks.configuration.AbstractConfiguration;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RestTwksClientConfiguration extends AbstractConfiguration {
    private final Optional<Integer> clientConnectTimeoutMs;
    private final Optional<Integer> clientReadTimeoutMs;
    private final Optional<Integer> clientWriteTimeoutMs;
    private final String serverBaseUrl;

    private RestTwksClientConfiguration(final Builder builder) {
        this.clientConnectTimeoutMs = builder.getClientConnectTimeoutMs();
        this.clientReadTimeoutMs = builder.getClientReadTimeoutMs();
        this.clientWriteTimeoutMs = builder.getClientWriteTimeoutMs();
        this.serverBaseUrl = builder.getServerBaseUrl();
    }

    public final static Builder builder() {
        return new Builder();
    }

    public final Optional<Integer> getClientConnectTimeoutMs() {
        return clientConnectTimeoutMs;
    }

    public final Optional<Integer> getClientReadTimeoutMs() {
        return clientReadTimeoutMs;
    }

    public final Optional<Integer> getClientWriteTimeoutMs() {
        return clientWriteTimeoutMs;
    }

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("clientConnectionTimeout", clientConnectTimeoutMs)
                .add("serverBaseUrl", serverBaseUrl);
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, RestTwksClientConfiguration> {
        private Optional<Integer> clientConnectTimeoutMs = Optional.absent();
        private Optional<Integer> clientReadTimeoutMs = Optional.absent();
        private Optional<Integer> clientWriteTimeoutMs = Optional.absent();
        private String serverBaseUrl = PropertyDefinitions.SERVER_BASE_URL.getDefault();

        @Override
        public final RestTwksClientConfiguration build() {
            return new RestTwksClientConfiguration(this);
        }

        public final Optional<Integer> getClientConnectTimeoutMs() {
            return clientConnectTimeoutMs;
        }

        public final Builder setClientConnectTimeoutMs(final Optional<Integer> clientConnectTimeoutMs) {
            this.clientConnectTimeoutMs = checkNotNull(clientConnectTimeoutMs);
            markDirty();
            return this;
        }

        public final Optional<Integer> getClientReadTimeoutMs() {
            return clientReadTimeoutMs;
        }

        public final Builder setClientReadTimeoutMs(final Optional<Integer> clientReadTimeoutMs) {
            this.clientReadTimeoutMs = clientReadTimeoutMs;
            markDirty();
            return this;
        }

        public final Optional<Integer> getClientWriteTimeoutMs() {
            return clientWriteTimeoutMs;
        }

        public final Builder setClientWriteTimeoutMs(final Optional<Integer> clientWriteTimeoutMs) {
            this.clientWriteTimeoutMs = clientWriteTimeoutMs;
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
            properties.getInteger(PropertyDefinitions.CLIENT_CONNECT_TIMEOUT_MS).ifPresent(value -> setClientConnectTimeoutMs(Optional.of(value)));
            properties.getInteger(PropertyDefinitions.CLIENT_READ_TIMEOUT_MS).ifPresent(value -> setClientReadTimeoutMs(Optional.of(value)));
            properties.getInteger(PropertyDefinitions.CLIENT_WRITE_TIMEOUT_MS).ifPresent(value -> setClientWriteTimeoutMs(Optional.of(value)));
            properties.getString(PropertyDefinitions.SERVER_BASE_URL).ifPresent(value -> setServerBaseUrl(value));
            return this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinition CLIENT_CONNECT_TIMEOUT_MS = new PropertyDefinition("clientConnectTimeoutMs");
        public final static PropertyDefinition CLIENT_READ_TIMEOUT_MS = new PropertyDefinition("clientReadTimeoutMs");
        public final static PropertyDefinition CLIENT_WRITE_TIMEOUT_MS = new PropertyDefinition("clientWriteTimeoutMs");
        public final static PropertyDefinitionWithDefault<String> SERVER_BASE_URL = new PropertyDefinitionWithDefault<>("http://localhost:8080", "serverBaseUrl");
    }
}
