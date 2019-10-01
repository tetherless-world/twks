package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationFactory;
import edu.rpi.tw.twdb.api.NanopublicationParser;
import edu.rpi.tw.twdb.api.Twdb;
import org.apache.jena.query.Dataset;
import org.dmfs.rfc3986.Uri;

import java.util.Optional;

public final class TwdbImpl implements Twdb {
    private final NanopublicationFactory nanopublicationFactory = new NanopublicationFactoryImpl();

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
    public NanopublicationFactory getNanopublicationFactory() {
        return nanopublicationFactory;
    }

    @Override
    public Dataset getNanopublicationsDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NanopublicationParser newNanopublicationParser() {
        return new NanopublicationParserImpl(nanopublicationFactory);
    }

    @Override
    public void putNanopublication(final Nanopublication nanopublication) {
        throw new UnsupportedOperationException();
    }
}
