package edu.rpi.tw.twks.text;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.configuration.AbstractConfiguration;

import java.nio.file.Path;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FullTextSearchConfiguration extends AbstractConfiguration {
    private final boolean enable;
    private final Optional<Path> luceneDirectoryPath;

    private FullTextSearchConfiguration(final Builder builder) {
        this.enable = builder.enable;
        this.luceneDirectoryPath = builder.luceneDirectoryPath;
    }

    public static Builder builder() {
        return new Builder();
    }

    public final boolean getEnable() {
        return enable;
    }

    public final Optional<Path> getLuceneDirectoryPath() {
        return luceneDirectoryPath;
    }

    @Override
    protected final MoreObjects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("enable", enable).add("luceneDirectoryPath", luceneDirectoryPath.orElse(null));
    }

    public final static class Builder extends AbstractConfiguration.Builder<Builder, FullTextSearchConfiguration> {
        private boolean enable = false;
        private Optional<Path> luceneDirectoryPath = Optional.empty();

        private Builder() {
        }

        @Override
        public FullTextSearchConfiguration build() {
            return new FullTextSearchConfiguration(this);
        }

        public final boolean getEnable() {
            return enable;
        }

        public final Optional<Path> getLuceneDirectoryPath() {
            return luceneDirectoryPath;
        }

        public final FullTextSearchConfiguration.Builder setEnable(final boolean enable) {
            this.enable = enable;
            markDirty();
            return this;
        }

        public final Builder setLuceneDirectoryPath(final Optional<Path> path) {
            this.luceneDirectoryPath = checkNotNull(path);
            markDirty();
            return this;
        }

        @Override
        public final Builder set(final ConfigurationWrapper properties) {
            properties.getPath(PropertyDefinitions.LUCENE_DIRECTORY_PATH).ifPresent(value -> setLuceneDirectoryPath(Optional.of(value)));
        }
    }

    private final static class PropertyDefinitions {
        public final static PropertyDefinitionWithDefault<Boolean> ENABLE = new PropertyDefinitionWithDefault<>(Boolean.FALSE, "enableFullTextSearch");
        public final static PropertyDefinition LUCENE_DIRECTORY_PATH = new PropertyDefinition("luceneDirectoryPath");
    }
}
