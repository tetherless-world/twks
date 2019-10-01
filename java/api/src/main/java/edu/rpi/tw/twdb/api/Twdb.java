package edu.rpi.tw.twdb.api;

import org.apache.jena.query.Dataset;
import org.dmfs.rfc3986.Uri;

import java.util.Optional;

public interface Twdb {
    boolean deleteNanopublication(Uri uri);

    Dataset getAssertionsDataset();

    Optional<Nanopublication> getNanopublication(Uri uri);

    NanopublicationFactory getNanopublicationFactory();

    Dataset getNanopublicationsDataset();

    NanopublicationParser newNanopublicationParser();

    void putNanopublication(Nanopublication nanopublication);
}
