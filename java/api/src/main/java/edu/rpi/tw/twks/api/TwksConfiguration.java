package edu.rpi.tw.twks.api;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwksConfiguration extends AbstractConfiguration {
    private final Path dumpDirectoryPath;

    protected TwksConfiguration(final Path dumpDirectoryPath) {
        this.dumpDirectoryPath = checkNotNull(dumpDirectoryPath);
    }

    public final Path getDumpDirectoryPath() {
        return dumpDirectoryPath;
    }

    @Override
    protected MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("dumpDirectoryPath", dumpDirectoryPath);
    }

    public abstract static class Builder<BuilderT extends Builder<?, ?>, TwksConfigurationT extends TwksConfiguration> extends AbstractConfiguration.Builder<BuilderT, TwksConfigurationT> {
        private Path dumpDirectoryPath = FieldDefinitions.DUMP_DIRECTORY_PATH.getDefault();

        @Override
        public abstract TwksConfigurationT build();

        public final Path getDumpDirectoryPath() {
            return dumpDirectoryPath;
        }

        @SuppressWarnings("unchecked")
        public final BuilderT setDumpDirectoryPath(final Path dumpDirectoryPath) {
            this.dumpDirectoryPath = dumpDirectoryPath;
            return (BuilderT) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public BuilderT setFromProperties(final Properties properties) {
            @Nullable final String dumpDirectoryPath = properties.getProperty(FieldDefinitions.DUMP_DIRECTORY_PATH.getPropertyKey());
            if (dumpDirectoryPath != null) {
                this.dumpDirectoryPath = Paths.get(dumpDirectoryPath);
            }

            return (BuilderT) this;
        }
    }

    private final static class FieldDefinitions {
        public final static ConfigurationFieldDefinitionWithDefault<Path> DUMP_DIRECTORY_PATH = new ConfigurationFieldDefinitionWithDefault<>(Paths.get("/dump"), "twks.dump");
    }
}
