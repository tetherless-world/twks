package edu.rpi.tw.twks.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TwksLibraryVersion {
    private static TwksLibraryVersion instance;
    private final int incremental, major, minor;
    private final String string;

    private TwksLibraryVersion() {
        int incremental;
        int major;
        int minor;
        String string;
        try (final InputStream inputStream = getClass().getResourceAsStream("version.properties")) {
            final Properties properties = new Properties();
            properties.load(inputStream);
            incremental = Integer.parseInt(properties.getProperty("incrementalVersion"));
            minor = Integer.parseInt(properties.getProperty("minorVersion"));
            major = Integer.parseInt(properties.getProperty("majorVersion"));
            string = properties.getProperty("version");
        } catch (final NumberFormatException e) {
            incremental = major = minor = 0;
            string = "";
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        this.incremental = incremental;
        this.major = major;
        this.minor = minor;
        this.string = string;
    }

    public final static synchronized TwksLibraryVersion getInstance() {
        if (instance == null) {
            instance = new TwksLibraryVersion();
        }
        return instance;
    }

    public final int getIncremental() {
        return incremental;
    }

    public final int getMajor() {
        return major;
    }

    public final int getMinor() {
        return minor;
    }

    @Override
    public final String toString() {
        return string;
    }
}
