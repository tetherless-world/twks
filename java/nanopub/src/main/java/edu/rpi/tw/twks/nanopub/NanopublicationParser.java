package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.PROV;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

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

    public final Nanopublication parse(final File filePath) throws MalformedNanopublicationException, IOException {
        final Uri nanopublicationUri = Uri.parse(checkNotNull(filePath).toURI().toString());
        rdfParserBuilder.source(filePath.getPath());
        return parseDelegate(Optional.of(nanopublicationUri));
    }

    public final Nanopublication parse(final Uri url) throws MalformedNanopublicationException, IOException {
        rdfParserBuilder.source(url.toString());
        return parseDelegate(Optional.of(url));
    }

    public final Nanopublication parse(final StringReader stringReader) throws MalformedNanopublicationException, IOException {
        return parse(stringReader, Optional.empty());
    }

    public final Nanopublication parse(final StringReader stringReader, final Optional<Uri> sourceUri) throws MalformedNanopublicationException, IOException {
        rdfParserBuilder.source(stringReader);
        return parseDelegate(sourceUri);
    }

    private Nanopublication parseDelegate(final Optional<Uri> sourceUri) throws MalformedNanopublicationException {
        final Dataset dataset = DatasetFactory.create();
        rdfParserBuilder.parse(dataset);

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        if (dataset.listNames().hasNext()) {
            return NanopublicationFactory.getInstance().createNanopublicationFromDataset(dataset, dialect);
        }

        // Can't assume the source URI can be extended with fragments, so create a new URI.
        final String nanopublicationUriString = "urn:uuid:" + UUID.randomUUID().toString();
        final Uri nanopublicationUri = Uri.parse(nanopublicationUriString);

        final Literal generatedAtTime = ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance()));

        final Model assertionModel = dataset.getDefaultModel();
        setNsPrefixes(assertionModel);
        final String assertionUriString = nanopublicationUriString + "#assertion";
        final Uri assertionUri = Uri.parse(assertionUriString);

        final Model provenanceModel = ModelFactory.createDefaultModel();
        setNsPrefixes(provenanceModel);
        provenanceModel.createResource(assertionUriString).addProperty(PROV.generatedAtTime, generatedAtTime);
        final Uri provenanceUri = Uri.parse(nanopublicationUriString + "#provenance");

        final Model publicationInfoModel = ModelFactory.createDefaultModel();
        setNsPrefixes(publicationInfoModel);
        final String publicationInfoUriString = nanopublicationUriString + "#publicationInfo";
        final Uri publicationInfoUri = Uri.parse(publicationInfoUriString);
        publicationInfoModel.createResource(nanopublicationUriString).addProperty(PROV.generatedAtTime, generatedAtTime);

        final String headUriString = nanopublicationUriString + "#head";
        final Uri headUri = Uri.parse(headUriString);
        final Model headModel = NanopublicationFactory.getInstance().createNanopublicationHead(assertionUri, nanopublicationUri, provenanceUri, publicationInfoUri);

        return new Nanopublication(new NanopublicationPart(assertionModel, assertionUri), new NanopublicationPart(headModel, headUri), new NanopublicationPart(provenanceModel, provenanceUri), new NanopublicationPart(publicationInfoModel, publicationInfoUri), nanopublicationUri);
    }
}
