package edu.rpi.tw.twks.nanopub;

import org.apache.jena.riot.Lang;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationParserBuilder {
    private NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;
    private boolean ignoreMalformedNanopublications = false;
    private Optional<Lang> lang = Optional.empty();

    public final NanopublicationParser build() {
        return new NanopublicationParser(dialect, ignoreMalformedNanopublications, lang);
    }

    public final NanopublicationParserBuilder setDialect(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
        return this;
    }

    public final NanopublicationParserBuilder setIgnoreMalformedNanopublications(final boolean ignoreMalformedNanopublications) {
        this.ignoreMalformedNanopublications = ignoreMalformedNanopublications;
        return this;
    }

    public final NanopublicationParserBuilder setLang(final Lang lang) {
        this.lang = Optional.of(lang);
        return this;
    }
}
