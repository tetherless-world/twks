package edu.rpi.tw.twdb.api;

import org.apache.jena.query.Dataset;

/**
 * Factory for nanopublications.
 */
public interface NanopublicationFactory {
    /**
     * Parse a well-formed nanopublication from a Dataset.
     * <p>
     * The Dataset is expected to contain only one nanopublication, and conform to the nanopublication
     * specification (http://nanopub.org/guidelines/working_draft/).
     */
    Nanopublication createNanopublicationFromDataset(Dataset dataset) throws InvalidNanopublicationException;
}
