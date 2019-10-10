package edu.rpi.tw.twks.nanopub.vocabulary;

import org.apache.jena.rdf.model.Model;

public final class Vocabularies {
    public static void setNsPrefixes(final Model model) {
        model.setNsPrefix(NANOPUB.PREFIX, NANOPUB.NS);
        model.setNsPrefix(PROV.PREFIX, PROV.NS);
    }
}
