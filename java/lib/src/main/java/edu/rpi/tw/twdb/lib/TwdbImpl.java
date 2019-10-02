package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.twdb.api.Twdb;
import org.apache.jena.query.Dataset;
import org.dmfs.rfc3986.Uri;

import java.util.Optional;

public final class TwdbImpl implements Twdb {
    @Override
    public boolean deleteNanopublication(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dataset getAssertionsDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dataset getNanopublicationsDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putNanopublication(final Nanopublication nanopublication) {
        throw new UnsupportedOperationException();
    }
}
