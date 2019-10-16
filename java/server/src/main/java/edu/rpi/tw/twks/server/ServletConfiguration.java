package edu.rpi.tw.twks.server;

import edu.rpi.tw.twks.factory.TwksConfiguration;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServletConfiguration extends TwksConfiguration {
    public final static String SERVER_BASE_URL_DEFAULT = "http://localhost:8080";

    private String serverBaseUrl = SERVER_BASE_URL_DEFAULT;

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
        setServerBaseUrl(properties.getProperty(PropertyKeys.SERVER_BASE_URL, serverBaseUrl));
        return this;
    }

    public final static class PropertyKeys extends TwksConfiguration.PropertyKeys {
        public final static String SERVER_BASE_URL = "twks.serverBaseUrl";
    }
}
