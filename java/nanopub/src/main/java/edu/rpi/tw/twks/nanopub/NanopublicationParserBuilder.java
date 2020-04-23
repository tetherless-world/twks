package edu.rpi.tw.twks.nanopub;

import com.codahale.metrics.MetricRegistry;
import org.apache.jena.riot.Lang;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationParserBuilder {
    private int concurrencyLevel = 1;
    private NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;
    private Optional<Lang> lang = Optional.empty();
    private Optional<MetricRegistry> metricRegistry = Optional.empty();

    public final NanopublicationParser build() {
        return new NanopublicationParser(concurrencyLevel, dialect, lang, metricRegistry.isPresent() ? metricRegistry.get() : new MetricRegistry());
    }

    public final NanopublicationParserBuilder setConcurrencyLevel(final int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
        return this;
    }

    public final NanopublicationParserBuilder setDialect(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
        return this;
    }

    public final NanopublicationParserBuilder setLang(final Lang lang) {
        this.lang = Optional.of(lang);
        return this;
    }

    public final NanopublicationParserBuilder setMetricRegistry(final MetricRegistry metricRegistry) {
        this.metricRegistry = Optional.of(metricRegistry);
        return this;
    }
}
