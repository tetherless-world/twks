package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationParser {
    private final RDFParserBuilder rdfParserBuilder = RDFParserBuilder.create();
    private NanopublicationDialect dialect = NanopublicationDialect.SPECIFICATION;

    public NanopublicationParser setDialect(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
        return this;
    }

    public final NanopublicationParser setLang(final Lang lang) {
        rdfParserBuilder.lang(checkNotNull(lang));
        return this;
    }

    public final Iterable<Nanopublication> parse(final File filePath) throws MalformedNanopublicationException, IOException {
        final Uri nanopublicationUri = Uri.parse(checkNotNull(filePath).toURI().toString());
        rdfParserBuilder.source(filePath.getPath());
        return parseDelegate(Optional.of(nanopublicationUri));
    }

    public final Iterable<Nanopublication> parse(final Uri url) throws MalformedNanopublicationException, IOException {
        rdfParserBuilder.source(url.toString());
        return parseDelegate(Optional.of(url));
    }

    public final Iterable<Nanopublication> parse(final StringReader stringReader) throws MalformedNanopublicationException, IOException {
        return parse(stringReader, Optional.empty());
    }

    public final Iterable<Nanopublication> parse(final StringReader stringReader, final Optional<Uri> sourceUri) throws MalformedNanopublicationException, IOException {
        rdfParserBuilder.source(stringReader);
        return parseDelegate(sourceUri);
    }

    private Iterable<Nanopublication> parseDelegate(final Optional<Uri> sourceUri) throws MalformedNanopublicationException {
        final Dataset dataset = DatasetFactory.create();
        rdfParserBuilder.parse(dataset);

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        if (dataset.listNames().hasNext()) {
            return NanopublicationFactory.getInstance().createNanopublicationsFromDataset(dataset, dialect);
        }

        return ImmutableList.of(NanopublicationFactory.getInstance().createNanopublicationFromAssertions(dataset.getDefaultModel()));
    }
}
