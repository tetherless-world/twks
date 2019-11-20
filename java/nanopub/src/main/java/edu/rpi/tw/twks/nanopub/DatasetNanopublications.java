package edu.rpi.tw.twks.nanopub;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.NANOPUB;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper class that facilitates iterating over the nanopublications within a Dataset within the confines of a Dataset transaction.
 * <p>
 * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
 */
public final class DatasetNanopublications implements AutoCloseableIterable<Nanopublication> {
    private final Dataset dataset;
    private final NanopublicationDialect dialect;
    private final boolean ownTransaction;
    private final DatasetTransaction transaction;

    public DatasetNanopublications(final Dataset dataset) {
        this(dataset, NanopublicationDialect.SPECIFICATION);
    }

    public DatasetNanopublications(final Dataset dataset, final NanopublicationDialect dialect) {
        this(dataset, dialect, true, new DatasetTransaction(dataset, ReadWrite.READ));
    }

    public DatasetNanopublications(final Dataset dataset, final DatasetTransaction transaction) {
        this(dataset, NanopublicationDialect.SPECIFICATION, transaction);
    }

    public DatasetNanopublications(final Dataset dataset, final NanopublicationDialect dialect, final DatasetTransaction transaction) {
        this(dataset, dialect, false, transaction);
    }

    private DatasetNanopublications(final Dataset dataset, final NanopublicationDialect dialect, final boolean ownTransaction, final DatasetTransaction transaction) {
        this.dataset = checkNotNull(dataset);
        this.dialect = checkNotNull(dialect);
        this.ownTransaction = ownTransaction;
        this.transaction = checkNotNull(transaction);
    }

    /**
     * Create one or more nanopublications from a Dataset containing the nanopublication parts' named graphs.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public static ImmutableList<Nanopublication> copyAll(final Dataset dataset) throws MalformedNanopublicationException {
        return copyAll(dataset, NanopublicationDialect.SPECIFICATION);
    }

    /**
     * Create one or more nanopublications from a Dataset containing the nanopublication parts' named graphs.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public static ImmutableList<Nanopublication> copyAll(final Dataset dataset, final NanopublicationDialect dialect) throws MalformedNanopublicationException {
        try (final DatasetNanopublications delegate = new DatasetNanopublications(dataset, dialect)) {
            try {
                return ImmutableList.copyOf(delegate);
            } catch (final MalformedNanopublicationRuntimeException e) {
                throw (MalformedNanopublicationException) e.getCause();
            }
        }
    }

    /**
     * Create a nanopublication from a Dataset containing the nanopublication parts' named graphs. The Dataset should only contain a single nanopublication.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public static Nanopublication copyOne(final Dataset dataset) throws MalformedNanopublicationException {
        return copyOne(dataset, NanopublicationDialect.SPECIFICATION);
    }

    /**
     * Create a nanopublication from a Dataset containing the nanopublication parts' named graphs. The Dataset should only contain a single nanopublication.
     * <p>
     * Does not modify the Dataset or its underlying Models, although they are passed as-is into Nanopublication and may be modified from there.
     */
    public static Nanopublication copyOne(final Dataset dataset, final NanopublicationDialect dialect) throws MalformedNanopublicationException {
        try (final DatasetNanopublications delegate = new DatasetNanopublications(dataset, dialect)) {
            try {
                return delegate.iterator().next();
            } catch (final MalformedNanopublicationRuntimeException e) {
                throw (MalformedNanopublicationException) e.getCause();
            }
        }
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

        if (partModel.isEmpty() && !dialect.allowEmptyPart()) {
            throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to an empty named graph (%s)", nanopublicationUri, partProperty, partResource));
        }

        if (!unusedDatasetModelNames.remove(partModelName) && !dialect.allowPartUriReuse()) {
            throw new MalformedNanopublicationException(String.format("nanopublication %s %s refers to a named graph that has already been used by another nanopublication", nanopublicationUri, partProperty, partResource));
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

        // Specification: All triples must be placed in one of [H] or [A] or [P] or [I]
        try {
            if (!dialect.allowDefaultModelStatements() && !dataset.getDefaultModel().isEmpty()) {
                throw new MalformedNanopublicationException("dataset contains statements in the default model");
            }

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

                    nanopublication = SpecificationNanopublicationDialect.createNanopublicationFromParts(assertion, head, nanopublicationUri, provenance, publicationInfo);
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
