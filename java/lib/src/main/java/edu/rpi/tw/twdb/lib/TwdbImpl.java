package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationFactory;
import edu.rpi.tw.twdb.api.NanopublicationParser;
import edu.rpi.tw.twdb.api.Twdb;
import org.apache.jena.query.Dataset;

import java.util.Optional;

public final class TwdbImpl implements Twdb {
    private final NanopublicationFactory nanopublicationFactory = new NanopublicationFactoryImpl();

    @Override
    public boolean deleteNanopublication(final String uri) {
        return false;
    }

    @Override
    public Dataset getAssertionsDataset() {
        return null;
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final String uri) {
        return Optional.empty();
    }

    @Override
    public NanopublicationFactory getNanopublicationFactory() {
        return nanopublicationFactory;
    }

    @Override
    public Dataset getNanopublicationsDataset() {
        return null;
    }

    @Override
    public NanopublicationParser newNanopublicationParser() {
        return new NanopublicationParserImpl(nanopublicationFactory);
    }

    @Override
    public void putNanopublication(final Nanopublication nanopublication) {

    }
}
