package edu.rpi.tw.twks.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public final class SIO {
    public final static String NS = "http://semanticscience.org/resource/";
    public final static String PREFIX = "sio";
    // Properties
    public final static Property isAbout = ResourceFactory.createProperty(NS + "isAbout");

    private SIO() {
    }
}
