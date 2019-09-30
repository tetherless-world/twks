package edu.rpi.tw.twdb.api;

public interface Nanopublication {
    NamedModel getAssertion();

    NamedModel getProvenance();

    NamedModel getPublicationInfo();

    String getUri();
}
