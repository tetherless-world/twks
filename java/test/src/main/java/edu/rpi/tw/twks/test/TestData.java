package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import org.apache.jena.riot.Lang;

import java.io.IOException;
import java.io.StringReader;

public final class TestData {
    // Copied from https://github.com/RDFLib/OWL-RL, W3C SOFTWARE NOTICE AND LICENSE
    public final static String RELATIVES_TTL = "@prefix : <http://example.org/relatives#> .\n" +
            "@prefix owl: <http://www.w3.org/2002/07/owl#> .\n" +
            "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
            "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n" +
            "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
            "@base <http://example.org/relatives> .\n" +
            "\n" +
            "<http://example.org/relatives> rdf:type owl:Ontology .\n" +
            "\n" +
            "#################################################################\n" +
            "#    Object Properties\n" +
            "#################################################################\n" +
            "\n" +
            "###  http://example.org/relatives#hasChild\n" +
            ":hasChild rdf:type owl:ObjectProperty ;\n" +
            "          owl:inverseOf :hasParent .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#hasParent\n" +
            ":hasParent rdf:type owl:ObjectProperty .\n" +
            "\n" +
            ":hasGrandparent rdf:type owl:ObjectProperty ;\n" +
            "                owl:propertyChainAxiom ( :hasParent\n" +
            "                                         :hasParent\n" +
            "                                       ) .\n" +
            "\n" +
            "#################################################################\n" +
            "#    Classes\n" +
            "#################################################################\n" +
            "\n" +
            "###  http://example.org/relatives#Child\n" +
            ":Child rdf:type owl:Class ;\n" +
            "       rdfs:subClassOf :Person .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Parent\n" +
            ":Parent rdf:type owl:Class ;\n" +
            "        rdfs:subClassOf :Person .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Person\n" +
            ":Person rdf:type owl:Class .\n" +
            "\n" +
            "\n" +
            "#################################################################\n" +
            "#    Individuals\n" +
            "#################################################################\n" +
            "\n" +
            "###  http://example.org/relatives#Aaron\n" +
            ":Aaron rdf:type owl:NamedIndividual .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Ann\n" +
            ":Ann rdf:type owl:NamedIndividual ,\n" +
            "              :Person .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Bill\n" +
            ":Bill rdf:type owl:NamedIndividual ,\n" +
            "               :Person .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Bob\n" +
            ":Bob rdf:type owl:NamedIndividual ,\n" +
            "              :Person ;\n" +
            "     :hasParent :Bill .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Cathy\n" +
            ":Cathy rdf:type owl:NamedIndividual ,\n" +
            "                :Person ;\n" +
            "       :hasParent :Bill .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Fred\n" +
            ":Fred rdf:type owl:NamedIndividual ,\n" +
            "               :Person ;\n" +
            "      :hasChild :James ;\n" +
            "      :hasParent :Cathy .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Jacob\n" +
            ":Jacob rdf:type owl:NamedIndividual ,\n" +
            "                :Person ;\n" +
            "       :hasParent :Fred .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#James\n" +
            ":James rdf:type owl:NamedIndividual ,\n" +
            "                :Person .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#James2\n" +
            ":James2 rdf:type owl:NamedIndividual ,\n" +
            "                 :Person ;\n" +
            "        :hasChild :John .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#John\n" +
            ":John rdf:type owl:NamedIndividual ,\n" +
            "               :Person ;\n" +
            "      :hasChild :Mary ,\n" +
            "                :Michael ;\n" +
            "      :hasParent :James2 .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Mary\n" +
            ":Mary rdf:type owl:NamedIndividual ,\n" +
            "               :Person .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Michael\n" +
            ":Michael rdf:type owl:NamedIndividual ,\n" +
            "                  :Person ;\n" +
            "         :hasParent :John .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Simon\n" +
            ":Simon rdf:type owl:NamedIndividual ,\n" +
            "                :Person ;\n" +
            "       :hasParent :Michael .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Tim\n" +
            ":Tim rdf:type owl:NamedIndividual ,\n" +
            "              :Person ;\n" +
            "     :hasParent :Simon ,\n" +
            "                :Valerie .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Valerie\n" +
            ":Valerie rdf:type owl:NamedIndividual ,\n" +
            "                  :Person .\n" +
            "\n" +
            "\n" +
            "###  http://example.org/relatives#Victor\n" +
            ":Victor rdf:type owl:NamedIndividual ,\n" +
            "                 :Person .\n" +
            "\n" +
            "\n" +
            "###  Generated by the OWL API (version 4.2.8.20170104-2310) https://github.com/owlcs/owlapi\n";

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

    //    public final File assertionOnlyFilePath;
//    public final File specNanopublicationFilePath;
//    public final Dataset specNanopublicationDataset = DatasetFactory.create();
    public final Nanopublication secondNanopublication;
    public final Nanopublication specNanopublication;
//    public final File whyisNanopublicationFilePath;

    public TestData() throws IOException, MalformedNanopublicationException {
//        assertionOnlyFilePath = getResourceFilePath("assertion_only.ttl");
//        specNanopublicationFilePath = getResourceFilePath("spec_nanopublication.trig");
//        parseDatasetFromResource(specNanopublicationDataset, "spec_nanopublication.trig");
//        whyisNanopublicationFilePath = getResourceFilePath("whyis_nanopublication.trig");
//        secondNanopublication = parseNanopublicationFromResource("second_nanopublication.trig");
//        specNanopublication = parseNanopublicationFromResource("spec_nanopublication.trig");
        // 20191011: pulling file resources is not working with the new -test module
        secondNanopublication = parseNanopublicationFromString(SECOND_NANOPUBLICATION_TRIG);
        specNanopublication = parseNanopublicationFromString(SPEC_NANOPUBLICATION_TRIG);
    }

    private Nanopublication parseNanopublicationFromString(final String trig) throws IOException, MalformedNanopublicationException {
        return new NanopublicationParser().setLang(Lang.TRIG).parse(new StringReader(trig));
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
