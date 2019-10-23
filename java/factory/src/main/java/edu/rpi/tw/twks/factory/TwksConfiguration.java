package edu.rpi.tw.twks.factory;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwksConfiguration {
    private Path dumpDirectoryPath = Paths.get("/dump");
    private Optional<String> tdb2Location = Optional.empty();

    public final Optional<String> getTdb2Location() {
        return tdb2Location;
    }

    public final TwksConfiguration setTdb2Location(final Optional<String> tdb2Location) {
        this.tdb2Location = checkNotNull(tdb2Location);
        return this;
    }

    public final Path getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    public final TwksConfiguration setDumpDirectoryPath(final Path dumpDirectoryPath) {
        this.dumpDirectoryPath = dumpDirectoryPath;
        return this;
    }

    public boolean isEmpty() {
        return !getTdb2Location().isPresent();
    }

    public final TwksConfiguration setFromSystemProperties() {
        return setFromProperties(System.getProperties());
    }

    public TwksConfiguration setFromProperties(final Properties properties) {
        {
            @Nullable final String dumpDirectoryPath = properties.getProperty(PropertyKeys.DUMP_DIRECTORY_PATH);
            if (dumpDirectoryPath != null) {
                this.dumpDirectoryPath = Paths.get(dumpDirectoryPath);
            }
        }

        {
            @Nullable final String tdb2Location = properties.getProperty(PropertyKeys.TDB2_LOCATION);
            if (tdb2Location != null) {
                this.tdb2Location = Optional.of(tdb2Location);
            }
        }

        return this;
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("dumpDirectoryPath", dumpDirectoryPath)
                .add("tdb2Location", tdb2Location.orElse(null));
    }

    public static class PropertyKeys {
        public final static String DUMP_DIRECTORY_PATH = "twks.dump";
        public final static String TDB2_LOCATION = "twks.tdbLocation";
    }
}
