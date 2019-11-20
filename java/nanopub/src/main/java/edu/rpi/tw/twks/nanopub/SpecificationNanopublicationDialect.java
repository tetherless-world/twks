package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public final class SpecificationNanopublicationDialect extends NanopublicationDialect {
    @Override
    final void validateNanopublicationParts(final NanopublicationPart assertion, final NanopublicationPart head, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
        checkNotNull(assertion);
        checkNotNull(head);
        checkNotNull(provenance);
        checkNotNull(publicationInfo);

        // Specification: The URIs for [N], [H], [A], [P], [I] must all be different
        checkUniqueParts(nanopublicationUri, head.getName(), assertion.getName(), provenance.getName(), publicationInfo.getName());

        // Specification: Triples in [P] have at least one reference to [A]
        if (!provenance.getModel().listStatements(ResourceFactory.createResource(assertion.getName().toString()), null, (String) null).hasNext()) {
            throw new MalformedNanopublicationException("provenance does not reference assertion graph");
        }

        final Resource nanopublicationResource = ResourceFactory.createResource(nanopublicationUri.toString());

        // Specification: Triples in [I] have at least one reference to [N]
        if (!publicationInfo.getModel().listStatements(nanopublicationResource, null, (String) null).hasNext()) {
            throw new MalformedNanopublicationException("publication info does not reference nanopublication");
        }

        checkOntology(assertion);
    }
}
