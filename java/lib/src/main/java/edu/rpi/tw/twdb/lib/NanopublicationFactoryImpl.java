package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

import java.util.List;

final class NanopublicationFactoryImpl implements NanopublicationFactory {
    @Override
    public Nanopublication createNanopublicationFromAssertion(final Model assertion) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset) {
        dataset.listNames().forEachRemaining((modelName) -> {
            final Model model = dataset.getNamedModel(modelName);
        });
        throw new UnsupportedOperationException();
    }
}
