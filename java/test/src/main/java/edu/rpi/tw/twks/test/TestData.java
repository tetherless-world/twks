package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;

public final class TestData {
    public final static String SECOND_NANOPUBLICATION_TRIG = "@prefix : <http://example.org/pub2#> .\n" +
            "@prefix ex: <http://example.org/> .\n" +
            "@prefix np:  <http://www.nanopub.org/nschema#> .\n" +
            "@prefix prov: <http://www.w3.org/ns/prov#> .\n" +
            "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n" +
            "\n" +
            ":head {\n" +
            "    ex:pub2 a np:Nanopublication .\n" +
            "    ex:pub2 np:hasAssertion :assertion .\n" +
            "    ex:pub2 np:hasProvenance :provenance .\n" +
            "    ex:pub2 np:hasPublicationInfo :pubInfo .\n" +
            "}\n" +
            "\n" +
            ":assertion {\n" +
            "    ex:aspirin ex:is-indicated-for ex:pain .\n" +
            "}\n" +
            "\n" +
            ":provenance {\n" +
            "    :assertion prov:generatedAtTime \"2012-02-03T14:38:00Z\"^^xsd:dateTime .\n" +
            "    :assertion prov:wasDerivedFrom :experiment .\n" +
            "    :assertion prov:wasAttributedTo :experimentScientist .\n" +
            "}\n" +
            "\n" +
            ":pubInfo {\n" +
            "    ex:pub2 prov:wasAttributedTo ex:paul .\n" +
            "    ex:pub2 prov:generatedAtTime \"2012-10-26T12:45:00Z\"^^xsd:dateTime .\n" +
            "}\n";
    public final static String SPEC_NANOPUBLICATION_TRIG = "@prefix : <http://example.org/pub1#> .\n" +
            "@prefix ex: <http://example.org/> .\n" +
            "@prefix np:  <http://www.nanopub.org/nschema#> .\n" +
            "@prefix prov: <http://www.w3.org/ns/prov#> .\n" +
            "@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .\n" +
            "\n" +
            ":head {\n" +
            "    ex:pub1 a np:Nanopublication .\n" +
            "    ex:pub1 np:hasAssertion :assertion .\n" +
            "    ex:pub1 np:hasProvenance :provenance .\n" +
            "    ex:pub1 np:hasPublicationInfo :pubInfo .\n" +
            "}\n" +
            "\n" +
            ":assertion {\n" +
            "    ex:trastuzumab ex:is-indicated-for ex:breast-cancer .\n" +
            "}\n" +
            "\n" +
            ":provenance {\n" +
            "    :assertion prov:generatedAtTime \"2012-02-03T14:38:00Z\"^^xsd:dateTime .\n" +
            "    :assertion prov:wasDerivedFrom :experiment .\n" +
            "    :assertion prov:wasAttributedTo :experimentScientist .\n" +
            "}\n" +
            "\n" +
            ":pubInfo {\n" +
            "    ex:pub1 prov:wasAttributedTo ex:paul .\n" +
            "    ex:pub1 prov:generatedAtTime \"2012-10-26T12:45:00Z\"^^xsd:dateTime .\n" +
            "}\n";
    private final static TestData instance = new TestData();
    //    public final File assertionOnlyFilePath;
//    public final File specNanopublicationFilePath;
//    public final Dataset specNanopublicationDataset = DatasetFactory.create();
    public final Nanopublication ontologyNanopublication;
    public final Uri ontologyUri = Uri.parse("http://example.com/ontology");
    public final Nanopublication secondNanopublication;
    public final Nanopublication secondOntologyNanopublication;
    public final Uri secondOntologyUri = Uri.parse("http://example.com/ontology2");
    //    public final File whyisNanopublicationFilePath;
    public final Nanopublication specNanopublication;

    public TestData() {
//        assertionOnlyFilePath = getResourceFilePath("assertion_only.ttl");
//        specNanopublicationFilePath = getResourceFilePath("spec_nanopublication.trig");
//        parseDatasetFromResource(specNanopublicationDataset, "spec_nanopublication.trig");
//        whyisNanopublicationFilePath = getResourceFilePath("whyis_nanopublication.trig");
//        secondNanopublication = parseNanopublicationFromResource("second_nanopublication.trig");
//        specNanopublication = parseNanopublicationFromResource("spec_nanopublication.trig");
        // 20191011: pulling file resources is not working with the new -test module
        try {
            secondNanopublication = parseNanopublicationFromString(SECOND_NANOPUBLICATION_TRIG);
            specNanopublication = parseNanopublicationFromString(SPEC_NANOPUBLICATION_TRIG);

            {
                final Model ontologyNanopublicationAssertions = ModelFactory.createDefaultModel().add(specNanopublication.getAssertion().getModel());
                ontologyNanopublicationAssertions.add(ResourceFactory.createResource(ontologyUri.toString()), RDF.type, OWL.Ontology);
                ontologyNanopublication = Nanopublication.builder().getAssertionBuilder().setModel(ontologyNanopublicationAssertions).getNanopublicationBuilder().build();
            }

            {
                final Model ontologyNanopublicationAssertions = ModelFactory.createDefaultModel().add(secondNanopublication.getAssertion().getModel());
                ontologyNanopublicationAssertions.add(ResourceFactory.createResource(secondOntologyUri.toString()), RDF.type, OWL.Ontology);
                secondOntologyNanopublication = Nanopublication.builder().getAssertionBuilder().setModel(ontologyNanopublicationAssertions).getNanopublicationBuilder().build();
            }
        } catch (final IOException | MalformedNanopublicationException e) {
            throw new RuntimeException(e);
        }
    }

    private Nanopublication parseNanopublicationFromString(final String trig) throws IOException, MalformedNanopublicationException {
        return NanopublicationParser.SPECIFICATION.parseString(trig).get(0);
    }

//    private Nanopublication parseNanopublicationFromResource(final String fileName) throws IOException, MalformedNanopublicationException {
//        final URL url = getClass().getResource("./" + fileName);
//        return new NanopublicationParser().parse(Uri.parse(url.toString()));
//    }

//    private File getResourceFilePath(final String fileName) throws IOException {
//        final URL url = getClass().getResource("./" + fileName);
//        try {
//            return new File(url.toURI());
//        } catch (final URISyntaxException e) {
//            throw new IOException(e);
//        }
//    }
//
//    private void parseDatasetFromResource(final Dataset dataset, final String fileName) throws IOException {
//        final URL url = getClass().getResource("./" + fileName);
//        try (final InputStream inputStream = url.openStream()) {
//            RDFParserBuilder.create().base(url.toString()).source(inputStream).parse(dataset);
//        }
//    }
}
