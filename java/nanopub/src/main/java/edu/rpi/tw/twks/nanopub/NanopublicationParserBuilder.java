package edu.rpi.tw.twks.nanopub;

import com.codahale.metrics.MetricRegistry;
import org.apache.jena.riot.Lang;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationParserBuilder {
    private NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;
    private Optional<Lang> lang = Optional.empty();
    private final Optional<MetricRegistry> metricRegistry = Optional.empty();

    public final NanopublicationParser build() {
        return new NanopublicationParser(dialect, lang, metricRegistry.isPresent() ? metricRegistry.get() : new MetricRegistry());
    }

    public final NanopublicationParserBuilder setDialect(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
        return this;
    }

    public final NanopublicationParserBuilder setLang(final Lang lang) {
        this.lang = Optional.of(lang);
        return this;
    }
}
