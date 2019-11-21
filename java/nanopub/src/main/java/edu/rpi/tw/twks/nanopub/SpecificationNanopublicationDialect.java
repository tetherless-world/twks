package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.NANOPUB;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

public final class SpecificationNanopublicationDialect extends NanopublicationDialect {
    private static void checkOntology(final NanopublicationPart assertion) throws MalformedNanopublicationException {
        // Reproducing Whyis functionality: if the assertions contain a statement of the form (?x a owl:Ontology), the publication info should have a statement (?np sio:isAbout ?x).
        for (final StmtIterator ontologyStatements = assertion.getModel().listStatements(null, RDF.type, OWL.Ontology); ontologyStatements.hasNext(); ) {
            final Statement ontologyStatement = ontologyStatements.nextStatement();
            final Resource ontologyResource = ontologyStatement.getSubject();
            if (ontologyResource.getURI() == null) {
                throw new MalformedNanopublicationException("assertion ?o a owl:Ontology must be about a non-blank node");
            }
            // 20191120: the sio:isAbout statement is no longer present.
//                final StmtIterator isAboutStatements = publicationInfo.getModel().listStatements(nanopublicationResource, SIO.isAbout, ontologyResource);
//                if (!isAboutStatements.hasNext()) {
//                    throw new MalformedNanopublicationException(String.format("assertion <%s> a owl:Ontology does not have corresponding publication info statement <%s> sio:isAbout <%s>", ontologyResource.getURI(), nanopublicationUri, ontologyResource.getURI()));
//                }
        }
    }

    private static void checkUniqueParts(final Uri... partUris) throws MalformedNanopublicationException {
        final Set<Uri> partUrisSet = new HashSet<>();
        for (final Uri partUri : partUris) {
            if (partUrisSet.contains(partUri)) {
                throw new MalformedNanopublicationException(String.format("duplicate part URI %s", partUri));
            }
            partUrisSet.add(partUri);
        }
    }

    /**
     * Create a nanopublication from its parts.
     * <p>
     * The caller must supply a URI/name for the head part, but the graph of the head will be automatically constructed.
     * <p>
     * Validates the parts but does not modify them. This method does most of the work of validating the contents of the nanopublication parts against the specification.
     */
    public final static Nanopublication createNanopublicationFromParts(final NanopublicationPart assertion, final Uri headUri, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
        checkNotNull(assertion);
        checkNotNull(headUri);
        checkNotNull(provenance);
        checkNotNull(publicationInfo);

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
    public final static Nanopublication createNanopublicationFromParts(final NanopublicationPart assertion, final NanopublicationPart head, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException {
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

        return new Nanopublication(assertion, head, provenance, publicationInfo, nanopublicationUri);
    }

    /**
     * Create the head part of a nanopublication from the other parts' URIs and the nanopublication URI.
     */
    private static Model createNanopublicationHead(final Uri assertionUri, final Uri nanopublicationUri, final Uri provenanceUri, final Uri publicationInfoUri) {
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

        // Reproducing Whyis functionality: explicit type statements that help storage
        assertionResource.addProperty(RDF.type, NANOPUB.Assertion);
        provenanceResource.addProperty(RDF.type, NANOPUB.Provenance);
        publicationInfoResource.addProperty(RDF.type, NANOPUB.PublicationInfo);

        return headModel;
    }

    @Override
    final boolean allowDefaultModelStatements() {
        return false;
    }

    @Override
    final boolean allowEmptyPart() {
        return false;
    }

    @Override
    final boolean allowPartUriReuse() {
        return false;
    }

    @Override
    public final Lang getDefaultLang() {
        return Lang.TRIG;
    }
}
