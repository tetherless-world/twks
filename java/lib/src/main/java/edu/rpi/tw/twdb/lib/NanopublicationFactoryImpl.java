package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationFactory;
import edu.rpi.tw.twdb.lib.vocabulary.NANOPUB;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

final class NanopublicationFactoryImpl implements NanopublicationFactory {
    @Override
    public Nanopublication createNanopublicationFromAssertion(final Model assertion) {
        return createNanopublicationFromAssertion(assertion, )
    }

    private Nanopublication createNanopublicationFromAssertion(final Model assertion, final String assertionUri) {
        return new NanopublicationImpl()
    }

    @Override
    public List<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset) {
        final List<Nanopublication> nanopublications = new ArrayList<>();

        // If there are any nanopublications in the dataset, only use those.
        dataset.getUnionModel().listSubjectsWithProperty(RDF.type, NANOPUB.Nanopublication).forEachRemaining((nanopublicationResource) -> {

        });

        if (!nanopublications.isEmpty()) {
            return nanopublications;
        }

        // Otherwise treat

        dataset.listNames().forEachRemaining((modelName) -> {
            final Model model = dataset.getNamedModel(modelName);
            // model.write(System.out, "TURTLE");
        });
        if (!dataset.getDefaultModel().isEmpty()) {
            nanopublications.add(createNanopublicationFromAssertion(dataset.getDefaultModel()));
        }

        return nanopublications;
    }
}
