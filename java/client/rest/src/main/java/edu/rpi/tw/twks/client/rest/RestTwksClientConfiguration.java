package edu.rpi.tw.twks.client.rest;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.configuration.AbstractConfiguration;

public final class RestTwksClientConfiguration extends AbstractConfiguration {
    private final String serverBaseUrl;

    private RestTwksClientConfiguration(final Builder builder) {
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
        return super.toStringHelper().add("serverBaseUrl", serverBaseUrl);
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, RestTwksClientConfiguration> {
        private String serverBaseUrl = PropertyDefinitions.SERVER_BASE_URL.getDefault();

        @Override
        public final RestTwksClientConfiguration build() {
            return new RestTwksClientConfiguration(this);
        }

        public final String getServerBaseUrl() {
            return serverBaseUrl;
        }

        public final Builder setServerBaseUrl(final String serverBaseUrl) {
            this.serverBaseUrl = serverBaseUrl;
            markDirty();
            return this;
        }

        @Override
        public final Builder set(final ConfigurationWrapper properties) {
            properties.getString(PropertyDefinitions.SERVER_BASE_URL).ifPresent(value -> setServerBaseUrl(value));
            return this;
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinitionWithDefault<String> SERVER_BASE_URL = new PropertyDefinitionWithDefault<>("http://localhost:8080", "serverBaseUrl");
    }
}
