package edu.rpi.tw.twks.vocabulary;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.*;

public final class Vocabularies {
    private Vocabularies() {
    }

    static void setNsPrefixes(final PrefixMapping prefixMapping) {
        prefixMapping.setNsPrefix("dc", DC_11.NS);
        prefixMapping.setNsPrefix("dcterms", DCTerms.NS);
        prefixMapping.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        prefixMapping.setNsPrefix("owl", OWL.NS);
        prefixMapping.setNsPrefix(PROV.PREFIX, PROV.NS);
        prefixMapping.setNsPrefix("rdf", RDF.uri);
        prefixMapping.setNsPrefix("rdfs", RDFS.uri);
        prefixMapping.setNsPrefix(SIO.PREFIX, SIO.NS);
        prefixMapping.setNsPrefix("skos", SKOS.uri);
        prefixMapping.setNsPrefix("xsd", XSD.getURI());
    }
}
