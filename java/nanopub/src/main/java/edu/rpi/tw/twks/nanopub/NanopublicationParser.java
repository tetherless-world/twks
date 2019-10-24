package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.File;
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

    public final Iterable<Nanopublication> parseAll(final File filePath) {
        final Uri nanopublicationUri = Uri.parse(checkNotNull(filePath).toURI().toString());
        rdfParserBuilder.source(filePath.getPath());
        return parseAllDelegate(Optional.of(nanopublicationUri));
    }

    public final Iterable<Nanopublication> parseAll(final Uri url) {
        rdfParserBuilder.source(url.toString());
        return parseAllDelegate(Optional.of(url));
    }

    public final Iterable<Nanopublication> parseAll(final StringReader stringReader) {
        return parseAll(stringReader, Optional.empty());
    }

    public final Iterable<Nanopublication> parseAll(final StringReader stringReader, final Optional<Uri> sourceUri) {
        rdfParserBuilder.source(stringReader);
        return parseAllDelegate(sourceUri);
    }

    public final Nanopublication parseOne(final File filePath) throws MalformedNanopublicationException {
        final Uri nanopublicationUri = Uri.parse(checkNotNull(filePath).toURI().toString());
        rdfParserBuilder.source(filePath.getPath());
        return parseOneDelegate(Optional.of(nanopublicationUri));
    }

    public final Nanopublication parseOne(final Uri url) throws MalformedNanopublicationException {
        rdfParserBuilder.source(url.toString());
        return parseOneDelegate(Optional.of(url));
    }

    public final Nanopublication parseOne(final StringReader stringReader) throws MalformedNanopublicationException {
        return parseOne(stringReader, Optional.empty());
    }

    public final Nanopublication parseOne(final StringReader stringReader, final Optional<Uri> sourceUri) throws MalformedNanopublicationException {
        rdfParserBuilder.source(stringReader);
        return parseOneDelegate(sourceUri);
    }

    private Iterable<Nanopublication> parseAllDelegate(final Optional<Uri> sourceUri) {
        final Dataset dataset = DatasetFactory.create();
        rdfParserBuilder.parse(dataset);

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        if (dataset.listNames().hasNext()) {
            return NanopublicationFactory.getInstance().createNanopublicationsFromDataset(dataset, dialect);
        }

        return ImmutableList.of(NanopublicationFactory.getInstance().createNanopublicationFromAssertions(dataset.getDefaultModel()));
    }

    private Nanopublication parseOneDelegate(final Optional<Uri> sourceUri) throws MalformedNanopublicationException {
        final Dataset dataset = DatasetFactory.create();
        rdfParserBuilder.parse(dataset);

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        if (dataset.listNames().hasNext()) {
            return NanopublicationFactory.getInstance().createNanopublicationFromDataset(dataset, dialect);
        }

        return NanopublicationFactory.getInstance().createNanopublicationFromAssertions(dataset.getDefaultModel());
    }
}
