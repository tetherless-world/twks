package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwksConfiguration {
    private final static Path DUMP_DIRECTORY_PATH_DEFAULT = Paths.get("/dump");
    private final Path dumpDirectoryPath;

    protected TwksConfiguration(final Path dumpDirectoryPath) {
        this.dumpDirectoryPath = checkNotNull(dumpDirectoryPath);
    }

    public static Builder builder() {
        return new Builder();
    }

    public final Path getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this).omitNullValues()
                .add("dumpDirectoryPath", dumpDirectoryPath);
    }

    public static class Builder {
        private Path dumpDirectoryPath = DUMP_DIRECTORY_PATH_DEFAULT;

        public TwksConfiguration build() {
            return new TwksConfiguration(dumpDirectoryPath);
        }

        public final Path getDumpDirectoryPath() {
            return dumpDirectoryPath;
        }

        public Builder setDumpDirectoryPath(final Path dumpDirectoryPath) {
            this.dumpDirectoryPath = dumpDirectoryPath;
            return this;
        }

        public Builder setFromSystemProperties() {
            return setFromProperties(System.getProperties());
        }

        public Builder setFromProperties(final Properties properties) {
            @Nullable final String dumpDirectoryPath = properties.getProperty(PropertyKeys.DUMP_DIRECTORY_PATH);
            if (dumpDirectoryPath != null) {
                this.dumpDirectoryPath = Paths.get(dumpDirectoryPath);
            }

            return this;
        }
    }

    public static class PropertyKeys {
        public final static String DUMP_DIRECTORY_PATH = "twks.dump";
    }
}
