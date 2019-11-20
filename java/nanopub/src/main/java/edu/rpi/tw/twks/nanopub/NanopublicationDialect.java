package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.util.HashSet;
import java.util.Set;

public abstract class NanopublicationDialect {
    // http://nanopub.org/guidelines/working_draft/
    public final static NanopublicationDialect SPECIFICATION = new SpecificationNanopublicationDialect();
    // https://github.com/tetherless-world/whyis
    public final static NanopublicationDialect WHYIS = new WhyisNanopublicationDialect();

    public static NanopublicationDialect valueOf(final String name) {
        switch (name.toUpperCase()) {
            case "SPECIFICATION":
                return SPECIFICATION;
            case "WHYIS":
                return WHYIS;
            default:
                throw new IllegalArgumentException(name);
        }
    }

    protected final void checkOntology(final NanopublicationPart assertion) throws MalformedNanopublicationException {
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

    protected final void checkUniqueParts(final Uri... partUris) throws MalformedNanopublicationException {
        final Set<Uri> partUrisSet = new HashSet<>();
        for (final Uri partUri : partUris) {
            if (partUrisSet.contains(partUri)) {
                throw new MalformedNanopublicationException(String.format("duplicate part URI %s", partUri));
            }
            partUrisSet.add(partUri);
        }
    }

    abstract void validateNanopublicationParts(final NanopublicationPart assertion, final NanopublicationPart head, final Uri nanopublicationUri, final NanopublicationPart provenance, final NanopublicationPart publicationInfo) throws MalformedNanopublicationException;
}
