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

    public final ImmutableList<Nanopublication> parseAll(final File filePath) throws MalformedNanopublicationException {
        final Uri nanopublicationUri = Uri.parse(checkNotNull(filePath).toURI().toString());
        rdfParserBuilder.source(filePath.getPath());
        return parseAllDelegate(Optional.of(nanopublicationUri));
    }

    public final ImmutableList<Nanopublication> parseAll(final Uri url) throws MalformedNanopublicationException {
        rdfParserBuilder.source(url.toString());
        return parseAllDelegate(Optional.of(url));
    }

    public final ImmutableList<Nanopublication> parseAll(final StringReader stringReader) throws MalformedNanopublicationException {
        return parseAll(stringReader, Optional.empty());
    }

    public final ImmutableList<Nanopublication> parseAll(final StringReader stringReader, final Optional<Uri> sourceUri) throws MalformedNanopublicationException {
        rdfParserBuilder.source(stringReader);
        return parseAllDelegate(sourceUri);
    }

    private ImmutableList<Nanopublication> parseAllDelegate(final Optional<Uri> sourceUri) throws MalformedNanopublicationException {
        final Dataset dataset = DatasetFactory.create();
        rdfParserBuilder.parse(dataset);

        final NanopublicationFactory factory = new NanopublicationFactory(dialect);

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        if (dataset.listNames().hasNext()) {
            return factory.createNanopublicationsFromDataset(dataset);
        }

        return ImmutableList.of(Nanopublication.builder().getAssertionBuilder().setModel(dataset.getDefaultModel()).getNanopublicationBuilder().build());
    }

    public final Nanopublication parseOne(final File filePath) throws MalformedNanopublicationException {
        return parseOneDelegate(parseAll(filePath));
    }

    public final Nanopublication parseOne(final Uri url) throws MalformedNanopublicationException {
        return parseOneDelegate(parseAll(url));
    }

    public final Nanopublication parseOne(final StringReader stringReader) throws MalformedNanopublicationException {
        return parseOne(stringReader, Optional.empty());
    }

    public final Nanopublication parseOne(final StringReader stringReader, final Optional<Uri> sourceUri) throws MalformedNanopublicationException {
        return parseOneDelegate(parseAll(stringReader, sourceUri));
    }

    private Nanopublication parseOneDelegate(final ImmutableList<Nanopublication> nanopublications) throws MalformedNanopublicationException {
        switch (nanopublications.size()) {
            case 0:
                throw new IllegalStateException();
            case 1:
                return nanopublications.get(0);
            default:
                throw new MalformedNanopublicationException("more than one nanopublication parsed");
        }
    }

    public NanopublicationParser setDialect(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
        return this;
    }

    public final NanopublicationParser setLang(final Lang lang) {
        rdfParserBuilder.lang(checkNotNull(lang));
        return this;
    }
}
