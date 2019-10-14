package edu.rpi.tw.twks.client;

import com.google.common.base.MoreObjects;

import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TwksClientConfiguration {
    public final static String BASE_URL_DEFAULT = "http://localhost:8080";

    private String baseUrl = BASE_URL_DEFAULT;

    public final String getBaseUrl() {
        return baseUrl;
    }

    public final TwksClientConfiguration setBaseUrl(final String baseUrl) {
        this.baseUrl = checkNotNull(baseUrl);
        return this;
    }

    public final TwksClientConfiguration setFromSystemProperties() {
        return setFromProperties(System.getProperties());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("baseUrl", baseUrl).toString();
    }

    public final TwksClientConfiguration setFromProperties(final Properties properties) {
        final String baseUrl = properties.getProperty(PropertyKeys.BASE_URL);
        if (baseUrl != null) {
            this.baseUrl = baseUrl;
        }
        return this;
    }

    public final static class PropertyKeys {
        public final static String BASE_URL = TwksClientConfiguration.class.getPackage().getName() + ".baseUrl";
    }

}
