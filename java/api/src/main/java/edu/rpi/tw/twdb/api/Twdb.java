package edu.rpi.tw.twdb.api;

import edu.rpi.tw.nanopub.Nanopublication;
import org.apache.jena.query.Dataset;
import org.dmfs.rfc3986.Uri;

import java.util.Optional;

public interface Twdb {
    boolean deleteNanopublication(Uri uri);

    Dataset getAssertionsDataset();

    Optional<Nanopublication> getNanopublication(Uri uri);

    Dataset getNanopublicationsDataset();

    void putNanopublication(Nanopublication nanopublication);
}
