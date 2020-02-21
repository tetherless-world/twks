package edu.rpi.tw.twks.nanopub;

import com.google.common.base.MoreObjects;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

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

    /**
     * Create a blank slate builder for a new nanopublication.
     */
    public static NanopublicationBuilder builder() {
        return new NanopublicationBuilder();
    }

    /**
     * Create a blank slate builder for a new nanopublication, with a custom URI.
     */
    public static NanopublicationBuilder builder(final Uri nanopublicationUri) {
        return new NanopublicationBuilder(nanopublicationUri);
    }

    /**
     * Get the assertion part of the nanopublication.
     */
    public final NanopublicationPart getAssertion() {
        return assertion;
    }

    /**
     * Get the head part of the nanopublication.
     */
    public final NanopublicationPart getHead() {
        return head;
    }

    /**
     * Get the provenance part of the nanopublication.
     */
    public final NanopublicationPart getProvenance() {
        return provenance;
    }

    /**
     * Get the publication info part of the nanopublication.
     */
    public final NanopublicationPart getPublicationInfo() {
        return publicationInfo;
    }

    /**
     * Get the URI of the nanopublication (?n a np:Nanopublication).
     */
    public final Uri getUri() {
        return uri;
    }

    /**
     * Is this nanopublication isomorphic with another nanopublication?
     * <p>
     * Tests isomorphism for each part.
     */
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

    /**
     * Create a new, default Dataset and add this nanopublication's parts (named graphs) to it.
     */
    public final Dataset toDataset() {
        final Dataset dataset = DatasetFactory.create();
        toDataset(dataset);
        return dataset;
    }

    /**
     * Add this nanopublication's parts to an existing Dataset.
     */
    public final void toDataset(final Dataset dataset) {
        checkNotNull(dataset);

        try (final DatasetTransaction transaction = new DatasetTransaction(dataset, ReadWrite.WRITE)) {
            toDataset(dataset, transaction);
            transaction.commit();
        }
    }

    /**
     * Add this nanopublication's parts to an existing Dataset within the scope of an existing Dataset transaction.
     */
    public final void toDataset(final Dataset dataset, final DatasetTransaction transaction) {
        checkNotNull(dataset);
        checkNotNull(transaction);

        for (final NanopublicationPart nanopublicationPart : new NanopublicationPart[]{getAssertion(), getHead(), getProvenance(), getPublicationInfo()}) {
            final String name = nanopublicationPart.getName().toString();
            if (dataset.containsNamedModel(nanopublicationPart.getName().toString())) {
                throw new DuplicateNanopublicationPartName(name);
            }
            dataset.addNamedModel(name, nanopublicationPart.getModel());
        }
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this)
                .add("uri", getUri())
                .add("assertion", getAssertion())
                .add("head", getHead())
                .add("provenance", getProvenance())
                .add("publicationInfo", getPublicationInfo())
                .toString();
    }

    /**
     * Serialize this nanopublication in Trig format.
     */
    public final void write(final OutputStream outputStream) {
        RDFDataMgr.write(outputStream, toDataset(), Lang.TRIG);
    }

    /**
     * Serialize this nanopublication in Trig format.
     */
    public final void write(final StringWriter writer) {
        RDFDataMgr.write(writer, toDataset(), Lang.TRIG);
    }

    /**
     * Serialize this nanopublication in Trig format.
     */
    public final String writeToString() throws IOException {
        try (final StringWriter stringWriter = new StringWriter()) {
            write(stringWriter);
            return stringWriter.toString();
        }
    }
}
