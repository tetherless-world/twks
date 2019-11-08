package edu.rpi.tw.twks.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class PROV {
    public final static String NS = "http://www.w3.org/ns/prov#";
    public final static String PREFIX = "prov";

    // Properties
    public final static Property generatedAtTime = ResourceFactory.createProperty(NS + "generatedAtTime");
    public final static Property wasDerivedFrom = ResourceFactory.createProperty(NS + "wasDerivedFrom");

}
