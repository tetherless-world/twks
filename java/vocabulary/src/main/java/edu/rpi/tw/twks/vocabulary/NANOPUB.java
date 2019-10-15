package edu.rpi.tw.twks.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public final class NANOPUB {
    public final static String PREFIX = "nanopub";
    public final static String NS = "http://www.nanopub.org/nschema#";

    // Properties
    public final static Property hasAssertion = ResourceFactory.createProperty(NS + "hasAssertion");
    public final static Property hasProvenance = ResourceFactory.createProperty(NS + "hasProvenance");
    public final static Property hasPublicationInfo = ResourceFactory.createProperty(NS + "hasPublicationInfo");

    // Resources
    public final static Resource Assertion = ResourceFactory.createResource(NS + "Assertion");
    public final static Resource Nanopublication = ResourceFactory.createResource(NS + "Nanopublication");
    public final static Resource Provenance = ResourceFactory.createResource(NS + "Provenance");
    public final static Resource PublicationInfo = ResourceFactory.createResource(NS + "PublicationInfo");
}
