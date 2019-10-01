package edu.rpi.tw.twdb.api;

import org.apache.jena.query.Dataset;

import java.util.Optional;

public interface Twdb {
    boolean deleteNanopublication(String uri);

    Dataset getAssertionsDataset();

    Optional<Nanopublication> getNanopublication(String uri);

    NanopublicationFactory getNanopublicationFactory();

    Dataset getNanopublicationsDataset();

    NanopublicationParser newNanopublicationParser();

    void putNanopublication(Nanopublication nanopublication);
}
