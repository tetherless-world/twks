package edu.rpi.tw.twks.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TwksVersion {
    public final static int INCREMENTAL;
    public final static int MAJOR;
    public final static int MINOR;
    public final static String STRING;

    static {
        int incremental;
        int major;
        int minor;
        String string;
        try (final InputStream inputStream = TwksVersion.class.getResourceAsStream("./version.properties")) {
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
        INCREMENTAL = incremental;
        MAJOR = major;
        MINOR = minor;
        STRING = string;
    }
}
