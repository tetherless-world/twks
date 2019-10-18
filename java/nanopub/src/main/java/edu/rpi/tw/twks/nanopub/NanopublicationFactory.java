package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.NANOPUB;
import edu.rpi.tw.twks.vocabulary.PROV;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

public final class NanopublicationFactory {
    private final static NanopublicationFactory instance = new NanopublicationFactory();

    private NanopublicationFactory() {
    }

    public static NanopublicationFactory getInstance() {
        return instance;
    }

    public final Nanopublication createNanopublicationFromAssertions(final Model assertions) {
        // Can't assume the source URI can be extended with fragments, so create a new URI.
        final String nanopublicationUriString = "urn:uuid:" + UUID.randomUUID().toString();
        final Uri nanopublicationUri = Uri.parse(nanopublicationUriString);

        final Literal generatedAtTime = ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance()));

        setNsPrefixes(assertions);
        final String assertionUriString = nanopublicationUriString + "#assertion";
        final Uri assertionUri = Uri.parse(assertionUriString);

        final Model provenanceModel = ModelFactory.createDefaultModel();
        setNsPrefixes(provenanceModel);
        provenanceModel.createResource(assertionUriString).addProperty(PROV.generatedAtTime, generatedAtTime);
        final Uri provenanceUri = Uri.parse(nanopublicationUriString + "#provenance");

        final Model publicationInfoModel = ModelFactory.createDefaultModel();
        setNsPrefixes(publicationInfoModel);
        final String publicationInfoUriString = nanopublicationUriString + "#publicationInfo";
        final Uri publicationInfoUri = Uri.parse(publicationInfoUriString);
        publicationInfoModel.createResource(nanopublicationUriString).addProperty(PROV.generatedAtTime, generatedAtTime);

        final String headUriString = nanopublicationUriString + "#head";
        final Uri headUri = Uri.parse(headUriString);
        final Model headModel = NanopublicationFactory.getInstance().createNanopublicationHead(assertionUri, nanopublicationUri, provenanceUri, publicationInfoUri);

        return new Nanopublication(new NanopublicationPart(assertions, assertionUri), new NanopublicationPart(headModel, headUri), new NanopublicationPart(provenanceModel, provenanceUri), new NanopublicationPart(publicationInfoModel, publicationInfoUri), nanopublicationUri);
    }

    public final ImmutableList<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset) throws MalformedNanopublicationException {
        return createNanopublicationsFromDataset(dataset, NanopublicationDialect.SPECIFICATION);
    }

    public final ImmutableList<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset, final NanopublicationDialect dialect) throws MalformedNanopublicationException {
        return new NanopublicationsFromDatasetFactory(dataset, dialect).createNanopublications();
    }

    public final Model createNanopublicationHead(final Uri assertionUri, final Uri nanopublicationUri, final Uri provenanceUri, final Uri publicationInfoUri) {
        checkNotNull(assertionUri);
        checkNotNull(nanopublicationUri);
        checkNotNull(provenanceUri);
        checkNotNull(publicationInfoUri);

        final Model headModel = ModelFactory.createDefaultModel();
        setNsPrefixes(headModel);

        final Resource assertionResource = headModel.createResource(assertionUri.toString());
        final Resource nanopublicationResource = headModel.createResource(nanopublicationUri.toString());
        final Resource provenanceResource = headModel.createResource(provenanceUri.toString());
        final Resource publicationInfoResource = headModel.createResource(publicationInfoUri.toString());

        // :head {
        //    ex:pub1 a np:Nanopublication .
        //    ex:pub1 np:hasAssertion :assertion .
        //    ex:pub1 np:hasProvenance :provenance .
        //    ex:pub1 np:hasPublicationInfo :pubInfo .
        //}
        nanopublicationResource.addProperty(RDF.type, NANOPUB.Nanopublication);
        nanopublicationResource.addProperty(NANOPUB.hasAssertion, assertionResource);
        nanopublicationResource.addProperty(NANOPUB.hasProvenance, provenanceResource);
        nanopublicationResource.addProperty(NANOPUB.hasPublicationInfo, publicationInfoResource);

        // Explicit type statements that help storage
        assertionResource.addProperty(RDF.type, NANOPUB.Assertion);
        provenanceResource.addProperty(RDF.type, NANOPUB.Provenance);
        publicationInfoResource.addProperty(RDF.type, NANOPUB.PublicationInfo);

        return headModel;
    }

    public final Nanopublication createNanopublicationFromParts(final NanopublicationPart assertion, final NanopublicationDialect dialect, final NanopublicationPart head, final NanopublicationPart provenance, final NanopublicationPart publicationInfo, final Uri uri) throws MalformedNanopublicationException {
        checkNotNull(assertion);
        checkNotNull(dialect);
        checkNotNull(head);
        checkNotNull(provenance);
        checkNotNull(publicationInfo);

        // The URIs for [N], [H], [A], [P], [I] must all be different
        final Set<Uri> partUrisSet = new HashSet<>(5);
        final Uri[] uniquePartUris;
        switch (dialect) {
            case SPECIFICATION:
                uniquePartUris = new Uri[]{uri, head.getName(), assertion.getName(), provenance.getName(), publicationInfo.getName()};
                break;
            case WHYIS:
                uniquePartUris = new Uri[]{uri, assertion.getName(), provenance.getName(), publicationInfo.getName()};
                if (!uri.equals(head.getName())) {
                    throw new MalformedNanopublicationException(String.format("expected Whyis head URI to be the same as the nanopublication URI (%s vs. %s)", head.getName(), uri));
                }
                break;
            default:
                throw new UnsupportedOperationException();
        }
        for (final Uri partUri : uniquePartUris) {
            if (partUrisSet.contains(partUri)) {
                throw new MalformedNanopublicationException(String.format("duplicate part URI %s", partUri));
            }
            partUrisSet.add(partUri);
        }

        if (dialect != NanopublicationDialect.WHYIS) {
            // Triples in [P] have at least one reference to [A]
            if (!provenance.getModel().listStatements(ResourceFactory.createResource(assertion.getName().toString()), null, (String) null).hasNext()) {
                throw new MalformedNanopublicationException("provenance does not reference assertion graph");
            }
        }

        // Triples in [I] have at least one reference to [N]
        if (!publicationInfo.getModel().listStatements(ResourceFactory.createResource(uri.toString()), null, (String) null).hasNext()) {
            throw new MalformedNanopublicationException("publication info does not reference nanopublication");
        }

        return new Nanopublication(assertion, head, provenance, publicationInfo, uri);
    }

    private final static class NanopublicationsFromDatasetFactory {
        private final Dataset dataset;
        private final NanopublicationDialect dialect;

        NanopublicationsFromDatasetFactory(final Dataset dataset, final NanopublicationDialect dialect) {
            this.dataset = checkNotNull(dataset);
            this.dialect = checkNotNull(dialect);
        }

        ImmutableList<Nanopublication> createNanopublications() throws MalformedNanopublicationException {
            try (final DatasetTransaction transaction = new DatasetTransaction(dataset, ReadWrite.READ)) {
                // All triples must be placed in one of [H] or [A] or [P] or [I]
                //        if (dataset.getDefaultModel().isEmpty()) {
                //            dataset.getDefaultModel().write(System.out, "TURTLE");
                //            throw new MalformedNanopublicationException("default model is not empty");
                //        }

                final Set<String> datasetModelNames = new HashSet<>();
                dataset.listNames().forEachRemaining(modelName -> {
                    if (datasetModelNames.contains(modelName)) {
                        throw new IllegalStateException();
                    }
                    datasetModelNames.add(modelName);
                });

                if (datasetModelNames.size() > 4) {
                    throw new MalformedNanopublicationException("dataset contains too many named graphs");
                }

                // Find the head graph.
                for (final String modelName : datasetModelNames) {
                    final NanopublicationPart head;
                    final Resource nanopublicationResource;
                    {
                        final Model model = dataset.getNamedModel(modelName);
                        final List<Resource> nanopublicationResources = model.listSubjectsWithProperty(RDF.type, NANOPUB.Nanopublication).toList();
                        switch (nanopublicationResources.size()) {
                            case 0:
                                continue;
                            case 1:
                                // Dropdown
                                break;
                            default:
                                // There is exactly one quad of the form '[N] rdf:type np:Nanopublication [H]', which identifies [N] as the nanopublication URI, and [H] as the head URI
                                throw new MalformedNanopublicationException(String.format("nanopublication head graph %s has more than one rdf:type Nanopublication", modelName));
                        }
                        head = new NanopublicationPart(model, Uri.parse(modelName));
                        nanopublicationResource = nanopublicationResources.get(0);
                        if (nanopublicationResource.getURI() == null) {
                            throw new MalformedNanopublicationException("nanopublication resource is a blank node");
                        }
                    }
                    // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasAssertion [A] [H]', which identifies [A] as the assertion URI
                    final NanopublicationPart assertion = getNanopublicationPart(head, nanopublicationResource, NANOPUB.hasAssertion, transaction);
                    // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasProvenance [P] [H]', which identifies [P] as the provenance URI
                    final NanopublicationPart provenance = getNanopublicationPart(head, nanopublicationResource, NANOPUB.hasProvenance, transaction);
                    // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasPublicationInfo [I] [H]', which identifies [I] as the publication information URI
                    final NanopublicationPart publicationInfo = getNanopublicationPart(head, nanopublicationResource, NANOPUB.hasPublicationInfo, transaction);

                    return ImmutableList.of(NanopublicationFactory.getInstance().createNanopublicationFromParts(assertion, dialect, head, provenance, publicationInfo, Uri.parse(nanopublicationResource.getURI())));
                }
            }

            throw new MalformedNanopublicationException("unable to locate head graph by rdf:type Nanopublication statement");
        }

        /**
         * Get the named model in a dataset that correspond to part of a nanopublication e.g., the named assertion graph.
         * The dataset contains
         * <nanopublication URI> nanopub:hasAssertion <assertion graph URI> .
         * The same goes for nanopub:hasProvenance and nanopub:hasPublicationInfo.
         */
        private NanopublicationPart getNanopublicationPart(final NanopublicationPart head, final Resource nanopublicationResource, final Property partProperty, final DatasetTransaction transaction) throws MalformedNanopublicationException {
            final List<RDFNode> partRdfNodes = head.getModel().listObjectsOfProperty(nanopublicationResource, partProperty).toList();

            switch (partRdfNodes.size()) {
                case 0:
                    throw new MalformedNanopublicationException(String.format("nanopublication %s has no %s", nanopublicationResource, partProperty));
                case 1:
                    break;
                default:
                    throw new MalformedNanopublicationException(String.format("nanopublication %s has more than one %s", nanopublicationResource, partProperty));
            }

            final RDFNode partRdfNode = partRdfNodes.get(0);

            if (!(partRdfNode instanceof Resource)) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s is not a resource", nanopublicationResource, partProperty));
            }

            final Resource partResource = (Resource) partRdfNode;

            if (partResource.getURI() == null) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s is a blank node", nanopublicationResource, partProperty));
            }


            final String partModelName = partResource.toString();
            final Model partModel = dataset.getNamedModel(partModelName);
            if (partModel == null) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to a missing named graph (%s)", nanopublicationResource, partProperty, partResource));
            }

            if (partModel.isEmpty()) {
                if (dialect != NanopublicationDialect.WHYIS) {
                    // Whyis nanopublications refer to parts (named graphs) that aren't present in the Dataset/.trig file.
                    throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to an empty named graph (%s)", nanopublicationResource, partProperty, partResource));
                }
            }

            final Uri partModelUri = Uri.parse(partModelName);

            return new NanopublicationPart(partModel, partModelUri);
        }
    }
}
