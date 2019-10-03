package edu.rpi.tw.twdb.api;

import edu.rpi.tw.nanopub.DatasetTransaction;
import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.nanopub.Nanopublication;
import org.apache.jena.query.Dataset;
import org.dmfs.rfc3986.Uri;

import java.util.Optional;

public interface Twdb {
    boolean deleteNanopublication(Uri uri);

    boolean deleteNanopublication(Uri uri, final DatasetTransaction transaction);

    Dataset getAssertionsDataset();

    Optional<Nanopublication> getNanopublication(Uri uri) throws MalformedNanopublicationException;

    Optional<Nanopublication> getNanopublication(Uri uri, DatasetTransaction transaction) throws MalformedNanopublicationException;

    Dataset getNanopublicationsDataset();

    void putNanopublication(Nanopublication nanopublication);

    void putNanopublication(Nanopublication nanopublication, DatasetTransaction transaction);
}
