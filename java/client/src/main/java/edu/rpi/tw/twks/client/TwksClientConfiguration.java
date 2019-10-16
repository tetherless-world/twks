package edu.rpi.tw.twks.client;

import com.google.common.base.MoreObjects;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwksClientConfiguration {
    public final static String SERVER_BASE_URL_DEFAULT = "http://localhost:8080";

    private String serverBaseUrl = SERVER_BASE_URL_DEFAULT;

    public final String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public final TwksClientConfiguration setServerBaseUrl(final String serverBaseUrl) {
        this.serverBaseUrl = checkNotNull(serverBaseUrl);
        return this;
    }

    public final TwksClientConfiguration setFromSystemProperties() {
        return setFromProperties(System.getProperties());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("serverBaseUrl", serverBaseUrl).toString();
    }

    public final TwksClientConfiguration setFromProperties(final Properties properties) {
        return setServerBaseUrl(properties.getProperty(PropertyKeys.SERVER_BASE_URL, serverBaseUrl));
    }

    public final static class PropertyKeys {
        public final static String SERVER_BASE_URL = "twks.serverBaseUrl";
    }
}
