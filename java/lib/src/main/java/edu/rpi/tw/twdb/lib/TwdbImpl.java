package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationFactory;
import edu.rpi.tw.twdb.api.Twdb;
import org.apache.jena.query.Dataset;

import java.util.Optional;

public final class TwdbImpl implements Twdb {

    @Override
    public boolean deleteNanopublication(String uri) {
        return false;
    }

    @Override
    public Dataset getAssertionsDataset() {
        return null;
    }

    @Override
    public Optional<Nanopublication> getNanopublication(String uri) {
        return Optional.empty();
    }

    @Override
    public NanopublicationFactory getNanopublicationFactory() {
        return null;
    }

    @Override
    public Dataset getNanopublicationsDataset() {
        return null;
    }

    @Override
    public void putNanopublication(Nanopublication nanopublication) {

    }
}
