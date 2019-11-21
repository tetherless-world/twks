package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.File;
import java.io.StringReader;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationParserBuilder {
    private final RDFParserBuilder rdfParserBuilder = RDFParserBuilder.create();
    private NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;
    private Optional<Lang> lang = Optional.empty();
    private Optional<Uri> sourceUri = Optional.empty();

    public final NanopublicationParser build() {
        if (!lang.isPresent()) {
            rdfParserBuilder.lang(dialect.getDefaultLang());
        }
        return new NanopublicationParser(dialect, rdfParserBuilder.build(), sourceUri);
    }

    public final NanopublicationParserBuilder setDialect(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
        return this;
    }

    public final NanopublicationParserBuilder setLang(final Lang lang) {
        this.lang = Optional.of(lang);
        rdfParserBuilder.lang(lang);
        return this;
    }

    public final NanopublicationParserBuilder setSource(final StringReader stringReader, final Optional<Uri> sourceUri) {
        rdfParserBuilder.source(stringReader);
        this.sourceUri = checkNotNull(sourceUri);
        return this;
    }

    public final NanopublicationParserBuilder setSource(final Uri url) {
        rdfParserBuilder.source(url.toString());
        this.sourceUri = Optional.of(url);
        return this;
    }

    public final NanopublicationParserBuilder setSource(final StringReader stringReader) {
        return setSource(stringReader, Optional.empty());
    }

    public final NanopublicationParserBuilder setSource(final File filePath) {
        rdfParserBuilder.source(filePath.getPath());
        this.sourceUri = Optional.of(Uri.parse(checkNotNull(filePath).toURI().toString()));
        return this;
    }

}
