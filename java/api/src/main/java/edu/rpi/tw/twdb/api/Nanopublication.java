package edu.rpi.tw.twdb.api;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

public interface Nanopublication {
    Model getAssertion();

    Model getProvenance();

    Model getPublicationInfo();

    String getUri();
}
