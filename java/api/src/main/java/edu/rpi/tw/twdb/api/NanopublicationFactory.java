package edu.rpi.tw.twdb.api;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

import java.util.List;

public interface NanopublicationFactory {
    Nanopublication createNanopublicationFromAssertion(Model assertion);

    List<Nanopublication> createNanopublicationsFromDataset(Dataset dataset);
}
