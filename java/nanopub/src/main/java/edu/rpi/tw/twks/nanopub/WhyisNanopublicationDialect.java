package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WhyisNanopublicationDialect extends NanopublicationDialect {
    @Override
    final void validateNanopublicationParts(final NanopublicationPart assertion, final NanopublicationPart head, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
        checkNotNull(assertion);
        checkNotNull(head);
        checkNotNull(provenance);
        checkNotNull(publicationInfo);

        checkUniqueParts(nanopublicationUri, assertion.getName(), provenance.getName(), publicationInfo.getName());
        checkOntology(assertion);
    }
}
