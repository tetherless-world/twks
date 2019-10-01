package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.*;
import edu.rpi.tw.twdb.lib.vocabulary.PROV;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParserBuilder;
import org.dmfs.rfc3986.Uri;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

final class NanopublicationParserImpl implements NanopublicationParser {
    private final NanopublicationFactory factory;
    private final RDFParserBuilder rdfParserBuilder = RDFParserBuilder.create();

    NanopublicationParserImpl(final NanopublicationFactory factory) {
        this.factory = factory;
    }

    @Override
    public NanopublicationParser setLang(final Lang lang) {
        rdfParserBuilder.lang(lang);
        return this;
    }

    @Override
    public Nanopublication parse(final File filePath) throws MalformedNanopublicationException, IOException {
        final Uri nanopublicationUri = Uris.parse(filePath.toURI().toString());
        rdfParserBuilder.source(filePath.getPath());
        return parseDelegate(nanopublicationUri);
    }

    @Override
    public Nanopublication parse(final Uri url) throws MalformedNanopublicationException, IOException {
        rdfParserBuilder.source(url.toString());
        return parseDelegate(url);
    }

    private Nanopublication parseDelegate(final Uri nanopublicationUri) throws IOException, MalformedNanopublicationException {
        final Dataset dataset = DatasetFactory.create();
        rdfParserBuilder.parse(dataset);

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        if (dataset.listNames().hasNext()) {
            return factory.createNanopublicationFromDataset(dataset);
        }

        final Literal generatedAtTime = ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance()));

        final String nanopublicationUriString = Uris.toString(nanopublicationUri);

        final Model assertion = dataset.getDefaultModel();
        final String assertionUriString = nanopublicationUriString + "#assertion";
        final Uri assertionUri = Uris.parse(assertionUriString);

        final Model provenance = ModelFactory.createDefaultModel();
        provenance.add(provenance.createResource(assertionUriString), PROV.generatedAtTime, generatedAtTime);
        final Uri provenanceUri = Uris.parse(nanopublicationUriString + "#provenance");

        final Model publicationInfo = ModelFactory.createDefaultModel();
        final String publicationInfoUriString = nanopublicationUriString + "#publicationInfo";
        final Uri publicationInfoUri = Uris.parse(publicationInfoUriString);
        publicationInfo.add(publicationInfo.createResource(publicationInfoUriString), PROV.generatedAtTime, generatedAtTime);

        return new NanopublicationImpl(new NamedModel(assertion, assertionUri), new NamedModel(provenance, provenanceUri), new NamedModel(publicationInfo, publicationInfoUri), nanopublicationUri);
    }
}
