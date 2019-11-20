package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.riot.Lang;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WhyisNanopublicationDialect extends NanopublicationDialect {
    @Override
    final boolean allowDefaultModelStatements() {
        return true;
    }

    @Override
    final boolean allowEmptyPart() {
        return true;
    }

    @Override
    final boolean allowPartUriReuse() {
        return true;
    }

    @Override
    public final Lang getDefaultLang() {
        return Lang.NQUADS;
    }

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
