package edu.rpi.tw.twdb.api;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import java.util.Optional;

public interface Twdb {
    boolean deleteNanopublication(String uri);

    Dataset getAssertionsDataset();

    Optional<Nanopublication> getNanopublication(String uri);

    NanopublicationParser getNanopublicationParser();

    Dataset getNanopublicationsDataset();

    void putNanopublication(Nanopublication nanopublication);
}
