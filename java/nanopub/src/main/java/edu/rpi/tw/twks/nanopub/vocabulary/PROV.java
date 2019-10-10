package edu.rpi.tw.twks.nanopub.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class PROV {
    public final static String PREFIX = "prov";
    public final static String NS = "http://www.w3.org/ns/prov#";

    // Properties
    public final static Property generatedAtTime = ResourceFactory.createProperty(NS + "wasGeneratedAtTime");
}
