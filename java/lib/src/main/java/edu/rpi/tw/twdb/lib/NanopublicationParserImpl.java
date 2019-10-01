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

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
        final String nanopublicationUri = filePath.toURI().toString();
        rdfParserBuilder.source(filePath.getPath());
        return parse(nanopublicationUri);
    }

    @Override
    public Nanopublication parse(final URL url) throws MalformedNanopublicationException, IOException {
        return null;
    }

    private Nanopublication parse(final String nanopublicationUri) throws IOException, MalformedNanopublicationException {
        final Dataset dataset = DatasetFactory.create();
        rdfParserBuilder.parse(dataset);

        // Dataset has named graphs, assume it's a well-formed nanopublication.
        if (dataset.listNames().hasNext()) {
            return factory.createNanopublicationFromDataset(dataset);
        }

        final Literal generatedAtTime = ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance()));

        final Model assertion = dataset.getDefaultModel();
        final String assertionUri = nanopublicationUri + "#assertion";

        final Model provenance = ModelFactory.createDefaultModel();
        provenance.add(provenance.createResource(assertionUri), PROV.generatedAtTime, generatedAtTime);
        final String provenanceUri = nanopublicationUri + "#provenance";

        final Model publicationInfo = ModelFactory.createDefaultModel();
        final String publicationInfoUri = nanopublicationUri + "#publicationInfo";
        publicationInfo.add(publicationInfo.createResource(publicationInfoUri), PROV.generatedAtTime, generatedAtTime);

        return new NanopublicationImpl(new NamedModel(assertion, assertionUri), new NamedModel(provenance, provenanceUri), new NamedModel(publicationInfo, publicationInfoUri), nanopublicationUri);
    }
}
