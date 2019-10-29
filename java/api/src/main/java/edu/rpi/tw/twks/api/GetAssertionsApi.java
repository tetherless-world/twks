package edu.rpi.tw.twks.api;

import org.apache.jena.rdf.model.Model;

public interface GetAssertionsApi {
    /**
     * Get a Model that contains the union of all assertion parts of stored nanopublications.
     * <p>
     * Writes to the returned Model will not be reflected in the underlying store.
     * <p>
     * The combining rule for assertions from different nanopublications is currently (201910) unspecified.
     */
    Model getAssertions();
}
