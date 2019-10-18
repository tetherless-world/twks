package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A nanopublication minimally consists of an assertion, the provenance of the assertion, and the provenance of the nanopublication.
 * <p>
 * Although this class exposes getters to mutable instance data, the latter should not be modified directly in most cases.
 * It is an artifact of Jena Models because mutable. The Nanopublication should be treated as immutable.
 * <p>
 * Nanopublications should be created with the NanopublicationBuilder.
 * <p>
 * Guidelines:
 * http://nanopub.org/guidelines/working_draft/
 */
public final class Nanopublication {
    private final NanopublicationPart assertion;
    private final NanopublicationPart head;
    private final NanopublicationPart provenance;
    private final NanopublicationPart publicationInfo;
    private final Uri uri;

    Nanopublication(final NanopublicationPart assertion, final NanopublicationPart head, final NanopublicationPart provenance, final NanopublicationPart publicationInfo, final Uri uri) {
        this.assertion = checkNotNull(assertion);
        this.head = checkNotNull(head);
        this.provenance = checkNotNull(provenance);
        this.publicationInfo = checkNotNull(publicationInfo);
        this.uri = checkNotNull(uri);
    }

    public static NanopublicationBuilder builder() {
        return new NanopublicationBuilder();
    }

    public final NanopublicationPart getAssertion() {
        return assertion;
    }

    final NanopublicationPart getHead() {
        return head;
    }

    public final NanopublicationPart getProvenance() {
        return provenance;
    }

    public final NanopublicationPart getPublicationInfo() {
        return publicationInfo;
    }

    public final Uri getUri() {
        return uri;
    }

    public final boolean isIsomorphicWith(final Nanopublication other) {
        checkNotNull(other);

        if (!getUri().equals(other.getUri())) {
            return false;
        }

        if (!getAssertion().isIsomorphicWith(other.getAssertion())) {
            return false;
        }

        if (!getHead().isIsomorphicWith(other.getHead())) {
            return false;
        }

        if (!getProvenance().isIsomorphicWith(other.getProvenance())) {
            return false;
        }

        if (!getPublicationInfo().isIsomorphicWith(other.getPublicationInfo())) {
            return false;
        }

        return true;
    }

    public final Dataset toDataset() {
        final Dataset dataset = DatasetFactory.create();
        toDataset(dataset);
        return dataset;
    }

    public final void toDataset(final Dataset dataset) {
        checkNotNull(dataset);

        try (final DatasetTransaction transaction = new DatasetTransaction(dataset, ReadWrite.WRITE)) {
            toDataset(dataset, transaction);
            transaction.commit();
        }
    }

    public final void toDataset(final Dataset dataset, final DatasetTransaction transaction) {
        checkNotNull(dataset);
        checkNotNull(transaction);

        for (final NanopublicationPart nanopublicationPart : new NanopublicationPart[]{getAssertion(), getHead(), getProvenance(), getPublicationInfo()}) {
            final String name = nanopublicationPart.getName().toString();
            if (dataset.containsNamedModel(nanopublicationPart.getName().toString())) {
                throw new DuplicateModelNameException(name);
            }
            dataset.addNamedModel(name, nanopublicationPart.getModel());
        }
    }
}
