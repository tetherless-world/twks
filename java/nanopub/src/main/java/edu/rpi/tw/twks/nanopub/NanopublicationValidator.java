package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationValidator {
    private final NanopublicationDialect dialect;

    NanopublicationValidator(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
    }

    public final void validateNanopublicationParts(final NanopublicationPart assertion, final NanopublicationPart head, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
        checkNotNull(assertion);
        checkNotNull(head);
        checkNotNull(provenance);
        checkNotNull(publicationInfo);

        // The URIs for [N], [H], [A], [P], [I] must all be different
        final Set<Uri> partUrisSet = new HashSet<>(5);
        final Uri[] uniquePartUris;
        switch (dialect) {
            case SPECIFICATION:
                uniquePartUris = new Uri[]{nanopublicationUri, head.getName(), assertion.getName(), provenance.getName(), publicationInfo.getName()};
                break;
            case WHYIS:
                uniquePartUris = new Uri[]{nanopublicationUri, assertion.getName(), provenance.getName(), publicationInfo.getName()};
                if (!nanopublicationUri.equals(head.getName())) {
                    throw new MalformedNanopublicationException(String.format("expected Whyis head URI to be the same as the nanopublication URI (%s vs. %s)", head.getName(), nanopublicationUri));
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
        if (!publicationInfo.getModel().listStatements(ResourceFactory.createResource(nanopublicationUri.toString()), null, (String) null).hasNext()) {
            throw new MalformedNanopublicationException("publication info does not reference nanopublication");
        }
    }
}
