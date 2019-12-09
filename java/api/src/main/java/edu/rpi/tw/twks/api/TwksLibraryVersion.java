package edu.rpi.tw.twks.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TwksLibraryVersion extends TwksVersion {
    private static TwksLibraryVersion instance;

    private TwksLibraryVersion(final int incremental, final int major, final int minor) {
        super(incremental, major, minor);
    }

    public final static synchronized TwksLibraryVersion getInstance() {
        if (instance == null) {
            try (final InputStream inputStream = TwksLibraryVersion.class.getResourceAsStream("version.properties")) {
                final Properties properties = new Properties();
                properties.load(inputStream);
                final int incremental = Integer.parseInt(properties.getProperty("incrementalVersion"));
                final int major = Integer.parseInt(properties.getProperty("majorVersion"));
                final int minor = Integer.parseInt(properties.getProperty("minorVersion"));
//                final String string = properties.getProperty("version");
                instance = new TwksLibraryVersion(incremental, major, minor);
            } catch (final NumberFormatException e) {
                instance = new TwksLibraryVersion(0, 0, 0);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
}
