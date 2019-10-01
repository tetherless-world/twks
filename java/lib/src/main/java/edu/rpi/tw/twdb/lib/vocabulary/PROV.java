package edu.rpi.tw.twdb.lib.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public final class PROV {
    public final static String PREFIX = "prov";
    public final static String NS = "http://www.w3.org/ns/prov#";

    // Properties
    public final static Property generatedAtTime = ResourceFactory.createProperty(NS + "wasGeneratedAtTime");
}
