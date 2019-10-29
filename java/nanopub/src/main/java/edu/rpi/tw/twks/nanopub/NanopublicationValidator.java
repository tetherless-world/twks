package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.SIO;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationValidator {
    private final NanopublicationDialect dialect;

    NanopublicationValidator(final NanopublicationDialect dialect) {
        this.dialect = checkNotNull(dialect);
    }

    final void validateNanopublicationParts(final NanopublicationPart assertion, final NanopublicationPart head, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
        checkNotNull(assertion);
        checkNotNull(head);
        checkNotNull(provenance);
        checkNotNull(publicationInfo);

        // Specification: The URIs for [N], [H], [A], [P], [I] must all be different
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
            // Specification: Triples in [P] have at least one reference to [A]
            if (!provenance.getModel().listStatements(ResourceFactory.createResource(assertion.getName().toString()), null, (String) null).hasNext()) {
                throw new MalformedNanopublicationException("provenance does not reference assertion graph");
            }
        }

        // Specification: Triples in [I] have at least one reference to [N]
        final Resource nanopublicationResource = ResourceFactory.createResource(nanopublicationUri.toString());
        if (!publicationInfo.getModel().listStatements(nanopublicationResource, null, (String) null).hasNext()) {
            throw new MalformedNanopublicationException("publication info does not reference nanopublication");
        }

        // Reproducing Whyis functionality: if the assertions contain a statement of the form (?x a owl:Ontology), the publication info should have a statement (?np sio:isAbout ?x).
        {
            for (final StmtIterator ontologyStatements = assertion.getModel().listStatements(null, RDF.type, OWL.Ontology); ontologyStatements.hasNext(); ) {
                final Statement ontologyStatement = ontologyStatements.nextStatement();
                final Resource ontologyResource = ontologyStatement.getSubject();
                if (ontologyResource.getURI() == null) {
                    throw new MalformedNanopublicationException("assertion ?o a owl:Ontology must be about a non-blank node");
                }
                final StmtIterator isAboutStatements = publicationInfo.getModel().listStatements(nanopublicationResource, SIO.isAbout, ontologyResource);
                if (!isAboutStatements.hasNext()) {
                    throw new MalformedNanopublicationException(String.format("assertion <%s> a owl:Ontology does not have corresponding publication info statement <%s> sio:isAbout <%s>", ontologyResource.getURI(), nanopublicationUri, ontologyResource.getURI()));
                }
            }
        }
    }
}
