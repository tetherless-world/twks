package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
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
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

public final class NanopublicationFactory {
    public final static NanopublicationFactory DEFAULT = new NanopublicationFactory();
    private final NanopublicationDialect dialect;
    private final NanopublicationValidator validator;

    public NanopublicationFactory() {
        this(NanopublicationDialect.SPECIFICATION);
    }

    public NanopublicationFactory(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
        this.validator = new NanopublicationValidator(dialect);
    }

    /**
     * Create a nanopublication from an assertions model. Fills in the provenance and publication info parts.
     * <p>
     * Does not modify the assertions Model, although it is passed to the Nanopublication as-is and may be modified from there.
     */
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
        final Model headModel = createNanopublicationHead(assertionUri, nanopublicationUri, provenanceUri, publicationInfoUri);

        return new Nanopublication(new NanopublicationPart(assertions, assertionUri), new NanopublicationPart(headModel, headUri), new NanopublicationPart(provenanceModel, provenanceUri), new NanopublicationPart(publicationInfoModel, publicationInfoUri), nanopublicationUri);
    }

    /**
     * Create a nanopublication from a Dataset containing the nanopublication parts' named graphs. The Dataset should only contain a single nanopublication.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public final Nanopublication createNanopublicationFromDataset(final Dataset dataset) throws MalformedNanopublicationException {
        final ImmutableList<Nanopublication> nanopublications = createNanopublicationsFromDataset(dataset);

        switch (nanopublications.size()) {
            case 0:
                throw new IllegalStateException();
            case 1:
                return nanopublications.get(0);
            default:
                throw new MalformedNanopublicationException("more than one nanopublication in dataset");
        }
    }

    /**
     * Create a nanopublication from its parts.
     * <p>
     * The caller must supply a URI/name for the head part, but the graph of the head will be automatically constructed.
     * <p>
     * Validates the parts but does not modify them. This method does most of the work of validating the contents of the nanopublication parts against the specification.
     */
    public final Nanopublication createNanopublicationFromParts(final NanopublicationPart assertion, final Uri headUri, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
        final NanopublicationPart head =
                new NanopublicationPart(
                        createNanopublicationHead(assertion.getName(), nanopublicationUri, provenance.getName(), publicationInfo.getName()),
                        headUri
                );
        return createNanopublicationFromParts(assertion, head, nanopublicationUri, provenance, publicationInfo);
    }

    /**
     * Create a nanopublication from parts.
     * <p>
     * This method is private because we don't want to allow callers to supply a head graph. We want to construct it.
     */
    private final Nanopublication createNanopublicationFromParts(final NanopublicationPart assertion, final NanopublicationPart head, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
        validator.validateNanopublicationParts(assertion, head, nanopublicationUri, provenance, publicationInfo);
        return new Nanopublication(assertion, head, provenance, publicationInfo, nanopublicationUri);
    }

    /**
     * Create the head part of a nanopublication from the other parts' URIs and the nanopublication URI.
     */
    private final Model createNanopublicationHead(final Uri assertionUri, final Uri nanopublicationUri, final Uri provenanceUri, final Uri publicationInfoUri) {
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

        // Specification:
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

    /**
     * Create one or more nanopublications from a Dataset containing the nanopublication parts' named graphs.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public final ImmutableList<Nanopublication> createNanopublicationsFromDataset(final Dataset dataset) throws MalformedNanopublicationException {
        // This method keeps a lot of state through a lot of logic, so delegate to a temporary instance that has all of the state
        try (final DatasetNanopublications delegate = new DatasetNanopublications(dataset)) {
            try {
                return ImmutableList.copyOf(delegate);
            } catch (final MalformedNanopublicationRuntimeException e) {
                throw (MalformedNanopublicationException) e.getCause();
            }
        }
    }

    /**
     * Iterate over one or more nanopublications from a Dataset containing the nanopublication parts' named graphs.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public final DatasetNanopublications iterateNanopublicationsFromDataset(final Dataset dataset, final DatasetTransaction transaction) {
        return new DatasetNanopublications(dataset, transaction);
    }

    /**
     * Iterate over one or more nanopublications from a Dataset containing the nanopublication parts' named graphs.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public final DatasetNanopublications iterateNanopublicationsFromDataset(final Dataset dataset) {
        return new DatasetNanopublications(dataset);
    }

    /**
     * Helper class that facilitates iterating over the nanopublications within a Dataset within the confines of a Dataset transaction.
     */
    public final class DatasetNanopublications implements AutoCloseable, Iterable<Nanopublication> {
        private final Dataset dataset;
        private final boolean ownTransaction;
        private final DatasetTransaction transaction;

        public DatasetNanopublications(final Dataset dataset) {
            this(dataset, true, new DatasetTransaction(dataset, ReadWrite.READ));
        }

        public DatasetNanopublications(final Dataset dataset, final DatasetTransaction transaction) {
            this(dataset, false, transaction);
        }

        private DatasetNanopublications(final Dataset dataset, final boolean ownTransaction, final DatasetTransaction transaction) {
            this.dataset = checkNotNull(dataset);
            this.ownTransaction = ownTransaction;
            this.transaction = checkNotNull(transaction);
        }

        @Override
        public void close() {
            if (ownTransaction) {
                transaction.close();
            }
        }

        /**
         * Get the head named graphs in the Dataset.
         *
         * @return a map of nanopublication URI -> head
         * @throws MalformedNanopublicationException
         */
        private Map<Uri, NanopublicationPart> getHeads(final Set<String> unusedDatasetModelNames) throws MalformedNanopublicationException {
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
                        // Specification: There is exactly one quad of the form '[N] rdf:type np:Nanopublication [H]', which identifies [N] as the nanopublication URI, and [H] as the head URI
                        throw new MalformedNanopublicationException(String.format("nanopublication head graph %s has more than one rdf:type Nanopublication", modelName));
                }
            }
            return headsByNanopublicationUri;
        }

        /**
         * Get the named model in a dataset that correspond to part of a nanopublication e.g., the named assertion graph.
         * The dataset contains
         * <nanopublication URI> nanopub:hasAssertion <assertion graph URI> .
         * The same goes for nanopub:hasProvenance and nanopub:hasPublicationInfo.
         */
        private NanopublicationPart getNanopublicationPart(final NanopublicationPart head, final Uri nanopublicationUri, final Property partProperty, final Set<String> unusedDatasetModelNames) throws MalformedNanopublicationException {
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

        @Override
        public Iterator<Nanopublication> iterator() {
            final Set<String> unusedDatasetModelNames = new HashSet<>();
            dataset.listNames().forEachRemaining(modelName -> {
                if (unusedDatasetModelNames.contains(modelName)) {
                    throw new IllegalStateException();
                }
                unusedDatasetModelNames.add(modelName);
            });

            final Iterator<Map.Entry<Uri, NanopublicationPart>> headEntryI;
            try {
                headEntryI = getHeads(unusedDatasetModelNames).entrySet().iterator();
            } catch (final MalformedNanopublicationException e) {
                throw new MalformedNanopublicationRuntimeException(e);
            }

            return new Iterator<Nanopublication>() {
                private @Nullable
                Nanopublication nanopublication = null;

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
                        // Specification: Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasAssertion [A] [H]', which identifies [A] as the assertion URI
                        final NanopublicationPart assertion = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasAssertion, unusedDatasetModelNames);
                        // Specification: Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasProvenance [P] [H]', which identifies [P] as the provenance URI
                        final NanopublicationPart provenance = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasProvenance, unusedDatasetModelNames);
                        // Specification: Given the nanopublication URI [N] and its head URI [H], there is exactly one quad of the form '[N] np:hasPublicationInfo [I] [H]', which identifies [I] as the publication information URI
                        final NanopublicationPart publicationInfo = getNanopublicationPart(head, nanopublicationUri, NANOPUB.hasPublicationInfo, unusedDatasetModelNames);

                        nanopublication = createNanopublicationFromParts(assertion, head, nanopublicationUri, provenance, publicationInfo);
                        return true;
                    } catch (final MalformedNanopublicationException e) {
                        throw new MalformedNanopublicationRuntimeException(e);
                    }
                }

                @Override
                public Nanopublication next() {
                    final Nanopublication nanopublication = checkNotNull(this.nanopublication);
                    this.nanopublication = null;
                    return nanopublication;
                }
            };
        }
    }
}
