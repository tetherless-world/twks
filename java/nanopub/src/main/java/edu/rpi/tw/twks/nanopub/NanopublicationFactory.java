package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.NANOPUB;
import edu.rpi.tw.twks.vocabulary.PROV;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
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

    public final Iterable<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset) {
        return createNanopublicationsFromDataset(dataset, NanopublicationDialect.SPECIFICATION);
    }

    public final Nanopublication createNanopublicationFromDataset(final Dataset dataset) throws MalformedNanopublicationException {
        return createNanopublicationFromDataset(dataset, NanopublicationDialect.SPECIFICATION);
    }

    public final Iterable<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset, final NanopublicationDialect dialect) {
        // This method keeps a lot of state through a lot of logic, so delegate to a temporary instance that has all of the state
        try (final NanopublicationsFromDatasetFactory delegate = new NanopublicationsFromDatasetFactory(dataset, dialect)) {
            return delegate.createNanopublications();
        }
    }

    public final Nanopublication createNanopublicationFromDataset(final Dataset dataset, final NanopublicationDialect dialect) throws MalformedNanopublicationException {
        try {
            final Iterator<Nanopublication> nanopublicationI = createNanopublicationsFromDataset(dataset, dialect).iterator();
            checkState(nanopublicationI.hasNext());
            final Nanopublication nanopublication = nanopublicationI.next();
            if (nanopublicationI.hasNext()) {
                throw new MalformedNanopublicationException("more than one nanopublication in dataset");
            }
            return nanopublication;
        } catch (final MalformedNanopublicationRuntimeException e) {
            throw (MalformedNanopublicationException) e.getCause();
        }
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

    private final static class NanopublicationsFromDatasetFactory implements AutoCloseable {
        private final Dataset dataset;
        private final NanopublicationDialect dialect;
        private final Set<String> unusedDatasetModelNames = new HashSet<>();
        private final DatasetTransaction transaction;

        NanopublicationsFromDatasetFactory(final Dataset dataset, final NanopublicationDialect dialect) {
            this.dataset = checkNotNull(dataset);
            this.dialect = checkNotNull(dialect);

            transaction = new DatasetTransaction(dataset, ReadWrite.READ);

            dataset.listNames().forEachRemaining(modelName -> {
                if (unusedDatasetModelNames.contains(modelName)) {
                    throw new IllegalStateException();
                }
                unusedDatasetModelNames.add(modelName);
            });
        }

        @Override
        public void close() {
            transaction.close();
        }

        /**
         * Get the head named graphs in the Dataset.
         *
         * @return a map of nanopublication URI -> head
         * @throws MalformedNanopublicationException
         */
        private Map<Uri, NanopublicationPart> getHeads() throws MalformedNanopublicationException {
            final Map<Uri, NanopublicationPart> headsByNanopublicationUri = new HashMap<>();
            for (final Iterator<String> unusedDatasetModelNameI = unusedDatasetModelNames.iterator(); unusedDatasetModelNameI.hasNext(); ) {
                final String modelName = unusedDatasetModelNameI.next();
                final Model model = dataset.getNamedModel(modelName);
                final List<Resource> nanopublicationResources = model.listSubjectsWithProperty(RDF.type, NANOPUB.Nanopublication).toList();
                switch (nanopublicationResources.size()) {
                    case 0:
                        continue;
                    case 1:
                        final Resource nanopublicationResource = nanopublicationResources.get(0);
                        if (nanopublicationResource.getURI() == null) {
                            throw new MalformedNanopublicationException("nanopublication resource is a blank node");
                        }
                        final Uri nanopublicationUri = Uri.parse(nanopublicationResource.getURI());
                        if (headsByNanopublicationUri.containsKey(nanopublicationUri)) {
                            throw new MalformedNanopublicationException(String.format("duplicate nanopublication URI %s", nanopublicationUri));
                        }
                        headsByNanopublicationUri.put(nanopublicationUri, new NanopublicationPart(model, Uri.parse(modelName)));
                        unusedDatasetModelNameI.remove();
                        break;
                    default:
                        // There is exactly one quad of the form '[N] rdf:type np:Nanopublication [H]', which identifies [N] as the nanopublication URI, and [H] as the head URI
                        throw new MalformedNanopublicationException(String.format("nanopublication head graph %s has more than one rdf:type Nanopublication", modelName));
                }
            }
            return headsByNanopublicationUri;
        }

        Iterable<Nanopublication> createNanopublications() {
            // All triples must be placed in one of [H] or [A] or [P] or [I]
            //        if (dataset.getDefaultModel().isEmpty()) {
            //            dataset.getDefaultModel().write(System.out, "TURTLE");
            //            throw new MalformedNanopublicationException("default model is not empty");
            //        }

            final Iterator<Map.Entry<Uri, NanopublicationPart>> headEntryI;
            try {
                headEntryI = getHeads().entrySet().iterator();
                if (!headEntryI.hasNext()) {
                    throw new MalformedNanopublicationException("unable to locate head graph by rdf:type Nanopublication statement");
                }
            } catch (final MalformedNanopublicationException e) {
                throw new MalformedNanopublicationRuntimeException(e);
            }

            return new Iterable<Nanopublication>() {
                @Override
                public Iterator<Nanopublication> iterator() {
                    return new Iterator<Nanopublication>() {
                        private @Nullable
                        Nanopublication nanopublication;

                        @Override
                        public boolean hasNext() {
                            if (nanopublication != null) {
                                return true;
                            }

                            if (!headEntryI.hasNext()) {
                                return false;
                            }

                            final Map.Entry<Uri, NanopublicationPart> headEntry = headEntryI.next();
                            final Uri nanopublicationUri = headEntry.getKey();
                            final NanopublicationPart head = headEntry.getValue();

                            try {
                                // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasAssertion [A] [H]', which identifies [A] as the assertion URI
                                final NanopublicationPart assertion = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasAssertion, transaction);
                                // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasProvenance [P] [H]', which identifies [P] as the provenance URI
                                final NanopublicationPart provenance = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasProvenance, transaction);
                                // Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasPublicationInfo [I] [H]', which identifies [I] as the publication information URI
                                final NanopublicationPart publicationInfo = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasPublicationInfo, transaction);

                                nanopublication = NanopublicationFactory.getInstance().createNanopublicationFromParts(assertion, dialect, head, provenance, publicationInfo, nanopublicationUri);
                                return true;
                            } catch (final MalformedNanopublicationException e) {
                                throw new MalformedNanopublicationRuntimeException(e);
                            }
                        }

                        @Override
                        public Nanopublication next() {
                            return checkNotNull(nanopublication);
                        }
                    };
                }
            };
        }

        /**
         * Get the named model in a dataset that correspond to part of a nanopublication e.g., the named assertion graph.
         * The dataset contains
         * <nanopublication URI> nanopub:hasAssertion <assertion graph URI> .
         * The same goes for nanopub:hasProvenance and nanopub:hasPublicationInfo.
         */
        private NanopublicationPart getNanopublicationPart(final NanopublicationPart head, final Uri nanopublicationUri, final Property partProperty, final DatasetTransaction transaction) throws MalformedNanopublicationException {
            final List<RDFNode> partRdfNodes = head.getModel().listObjectsOfProperty(ResourceFactory.createResource(nanopublicationUri.toString()), partProperty).toList();

            switch (partRdfNodes.size()) {
                case 0:
                    throw new MalformedNanopublicationException(String.format("nanopublication %s has no %s", nanopublicationUri, partProperty));
                case 1:
                    break;
                default:
                    throw new MalformedNanopublicationException(String.format("nanopublication %s has more than one %s", nanopublicationUri, partProperty));
            }

            final RDFNode partRdfNode = partRdfNodes.get(0);

            if (!(partRdfNode instanceof Resource)) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s is not a resource", nanopublicationUri, partProperty));
            }

            final Resource partResource = (Resource) partRdfNode;

            if (partResource.getURI() == null) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s is a blank node", nanopublicationUri, partProperty));
            }

            final String partModelName = partResource.toString();

            final Model partModel = dataset.getNamedModel(partModelName);
            if (partModel == null) {
                throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to a missing named graph (%s)", nanopublicationUri, partProperty, partResource));
            }

            if (!unusedDatasetModelNames.remove(partModelName)) {
                if (dialect != NanopublicationDialect.WHYIS) {
                    throw new MalformedNanopublicationException(String.format("nanopublication %s %s refern to a named graph that has already been used by another nanopublication", nanopublicationUri, partProperty, partResource));
                }
            }

            if (partModel.isEmpty()) {
                if (dialect != NanopublicationDialect.WHYIS) {
                    // Whyis nanopublications refer to parts (named graphs) that aren't present in the Dataset/.trig file.
                    throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to an empty named graph (%s)", nanopublicationUri, partProperty, partResource));
                }
            }

            final Uri partModelUri = Uri.parse(partModelName);

            return new NanopublicationPart(partModel, partModelUri);
        }
    }
}
