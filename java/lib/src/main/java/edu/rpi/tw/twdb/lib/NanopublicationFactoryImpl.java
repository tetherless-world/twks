package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.twdb.api.InvalidNanopublicationException;
import edu.rpi.tw.twdb.api.NamedModel;
import edu.rpi.tw.twdb.api.Nanopublication;
import edu.rpi.tw.twdb.api.NanopublicationFactory;
import edu.rpi.tw.twdb.lib.vocabulary.NANOPUB;
import edu.rpi.tw.twdb.lib.vocabulary.TWDB;
import jdk.internal.jline.internal.Nullable;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class NanopublicationFactoryImpl implements NanopublicationFactory {
    @Override
    public Nanopublication createNanopublicationFromAssertion(final Model assertion) {
        final String nanopublicationUri = newNanopublicationUri();
    }

    private Nanopublication createNanopublicationFromAssertion(final NamedModel assertion) {
    }

    private static String newNanopublicationUri() {
        return TWDB.NS + UUID.randomUUID().toString();
    }

    private List<Nanopublication> createNanopublicationsFromAssertions(final Dataset dataset) {
        final List<Nanopublication> nanopublications = new ArrayList<>();

        dataset.listNames().forEachRemaining((modelName) -> {
            final Model model = dataset.getNamedModel(modelName);
            nanopublications.add(createNanopublicationFromAssertion(new NamedModel(model, modelName)));
        });
        if (!dataset.getDefaultModel().isEmpty()) {
            nanopublications.add(createNanopublicationFromAssertion(dataset.getDefaultModel()));
        }

        return nanopublications;
    }


    @Override
    public List<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset) {
        // If there are any nanopublications in the dataset, only use those.
        final List<Resource> nanopublicationResources = dataset.getUnionModel().listSubjectsWithProperty(RDF.type, NANOPUB.Nanopublication).toList();
        if (!nanopublicationResources.isEmpty()) {
            return createNanopublicationsFromNanopublicationResources(dataset, nanopublicationResources);
        }

        // Otherwise treat any models in the dataset as assertions
        return createNanopublicationsFromAssertions(dataset);
    }

    private static List<Nanopublication> createNanopublicationsFromNanopublicationResources(final Dataset dataset, final List<Resource> nanopublicationResources) {
        final List<Nanopublication> nanopublications = new ArrayList<>();
        nanopublicationResources.forEach(nanopublicationResource -> {
            final List<NamedModel> assertions = getNanopublicationPartsFromDataset(dataset, nanopublicationResource, NANOPUB.hasAssertion).toList();
        });
        return nanopublications;
    }

    /**
     * Get the named models in a dataset that correspond to part of a nanopublication e.g., the named assertion graphs.
     * The dataset contains
     * <nanopublication URI> nanopub:hasAssertion <assertion graph URI> .
     * The same goes for nanopub:hasProvenance and nanopub:hasPublicationInfo
     */
    private static NamedModel getNanopublicationPartFromDataset(final Dataset dataset, final Resource nanopublicationResource, final Property partProperty) throws InvalidNanopublicationException {
        List<RDFNode> partResources = dataset.getUnionModel().listObjectsOfProperty(nanopublicationResource, partProperty).toList();
        if (partResources.isEmpty()) {
            throw new InvalidNanopublicationException(String.format("nanopublication %s has no %s", nanopublicationResource, partProperty));
        }



        if (!(partResource.isURIResource())) {
            throw new InvalidNanopublicationException(String.format("nanopublication %s %s is not a URI resource"))
        }

        dataset.getUnionModel().listObjectsOfProperty(nanopublicationResource, partProperty).mapWith(partResource -> {
            final String partModelName = partResource.toString();
            @Nullable Model partModel = dataset.getNamedModel(partModelName);
            if (partModel == null) {
                partModel = ModelFactory.createDefaultModel();
            }
            return new NamedModel(partModel, partModelName);
        });
    }

}
