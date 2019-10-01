package edu.rpi.tw.twdb.api;

import org.apache.jena.query.Dataset;

/**
 * Factory for nanopublications.
 */
public interface NanopublicationFactory {
    /**
     * Parse a well-formed nanopublication from a Dataset.
     * The Dataset is expected to contain only one nanopublication, and conform to the nanopublication
     * specification (http://nanopub.org/guidelines/working_draft/).
     */
    Nanopublication createNanopublicationFromDataset(Dataset dataset) throws MalformedNanopublicationException;

    /**
     * Create a well-formed nanopublication from its parts.
     * The parts are expected to conform to the nanopublication specification (http://nanopub.org/guidelines/working_draft/).
     */
    Nanopublication createNanopublicationFromParts(NamedModel assertion, NamedModel provenance, NamedModel publicationInfo, String uri) throws MalformedNanopublicationException;
}
