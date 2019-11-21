package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.PROV;
import edu.rpi.tw.twks.vocabulary.SIO;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.util.Calendar;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

public final class NanopublicationBuilder {
    private final NanopublicationAssertionBuilder assertionBuilder;
    private final Resource assertionResource;
    private final Resource nanopublicationResource;
    private final Uri nanopublicationUri;
    private final NanopublicationProvenanceBuilder provenanceBuilder;
    private final Resource provenanceResource;
    private final NanopublicationPublicationInfoBuilder publicationInfoBuilder;
    private final Resource publicationInfoResource;

    NanopublicationBuilder() {
        this(Uri.parse("urn:uuid:" + UUID.randomUUID().toString()));
    }

    NanopublicationBuilder(final Uri nanopublicationUri) {
        // The nanopublication part names (URIs) are all dereived from the nanopublication URI.
        // Allowing custom part URIs is not needed in what's supposed to be a convenience builder.
        this.nanopublicationUri = checkNotNull(nanopublicationUri);

        nanopublicationResource = ResourceFactory.createResource(nanopublicationUri.toString());

        assertionResource = ResourceFactory.createResource(nanopublicationUri.toString() + "#assertion");
        assertionBuilder = new NanopublicationAssertionBuilder();

        provenanceResource = ResourceFactory.createResource(nanopublicationUri.toString() + "#provenance");
        provenanceBuilder = new NanopublicationProvenanceBuilder();

        publicationInfoResource = ResourceFactory.createResource(nanopublicationUri.toString() + "#publicationInfo");
        publicationInfoBuilder = new NanopublicationPublicationInfoBuilder();
    }

    public final Nanopublication build() throws MalformedNanopublicationException {
        final NanopublicationPart assertion = assertionBuilder.build();
        final Uri headUri = Uri.parse(nanopublicationUri.toString() + "#head");
        final NanopublicationPart provenance = provenanceBuilder.build();
        final NanopublicationPart publicationInfo = publicationInfoBuilder.build(assertion.getModel());
        return SpecificationNanopublicationDialect.createNanopublicationFromParts(assertion, headUri, this.nanopublicationUri, provenance, publicationInfo);
    }

    public final NanopublicationAssertionBuilder getAssertionBuilder() {
        return assertionBuilder;
    }

    public final NanopublicationProvenanceBuilder getProvenanceBuilder() {
        return provenanceBuilder;
    }

    public final NanopublicationPublicationInfoBuilder getPublicationInfoBuilder() {
        return publicationInfoBuilder;
    }

    private enum NanopublicationPartType {
        ASSERTION,
        PROVENANCE,
        PUBLICATION_INFO
    }

    public final class NanopublicationAssertionBuilder extends NanopublicationPartBuilder<NanopublicationAssertionBuilder> {
        private NanopublicationAssertionBuilder() {
        }

        private NanopublicationPart build() throws MalformedNanopublicationException {
            if (!getModel().listStatements().hasNext()) {
                throw new MalformedNanopublicationException("no assertion statements");
            }

            return new NanopublicationPart(getModel(), Uri.parse(assertionResource.getURI()));
        }

        @Override
        protected final NanopublicationPartType getType() {
            return NanopublicationPartType.ASSERTION;
        }
    }

    private abstract class NanopublicationPartBuilder<NanopublicationPartBuilderT extends NanopublicationPartBuilder<?>> {
        private Model model;

        private NanopublicationPartBuilder() {
            this.setModel(ModelFactory.createDefaultModel());
        }

        public final Model getModel() {
            return model;
        }

        @SuppressWarnings("unchecked")
        public NanopublicationPartBuilderT setModel(final Model model) {
            this.model = checkNotNull(model);
            setNsPrefixes(model);
            return (NanopublicationPartBuilderT) this;
        }

        public final NanopublicationBuilder getNanopublicationBuilder() {
            return NanopublicationBuilder.this;
        }

        protected abstract NanopublicationPartType getType();
    }

    public final class NanopublicationProvenanceBuilder extends NanopublicationPartBuilder<NanopublicationProvenanceBuilder> {
        private NanopublicationProvenanceBuilder() {
        }

        NanopublicationPart build() {
            if (!getModel().listStatements(assertionResource, null, (String) null).hasNext()) {
                getModel().add(assertionResource, PROV.generatedAtTime, ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance())));
            }
            return new NanopublicationPart(getModel(), Uri.parse(provenanceResource.getURI()));
        }

        @Override
        protected final NanopublicationPartType getType() {
            return NanopublicationPartType.PROVENANCE;
        }

        public final NanopublicationProvenanceBuilder wasDerivedFrom(final Uri uri) {
            getModel().add(assertionResource, PROV.wasDerivedFrom, getModel().createResource(uri.toString()));
            return this;
        }
    }

    public final class NanopublicationPublicationInfoBuilder extends NanopublicationPartBuilder<NanopublicationPublicationInfoBuilder> {
        private NanopublicationPublicationInfoBuilder() {
        }

        NanopublicationPart build(final Model assertionModel) {
            // Reproducing Whyis functionality: if the assertions contain a statement of the form (?x a owl:Ontology), the publication info should have a statement (?np sio:isAbout ?x).
            {
                for (final StmtIterator ontologyStatements = assertionModel.listStatements(null, RDF.type, OWL.Ontology); ontologyStatements.hasNext(); ) {
                    final Statement ontologyStatement = ontologyStatements.nextStatement();
                    final Resource ontologyResource = ontologyStatement.getSubject();
                    if (ontologyResource.getURI() == null) {
                        continue;
                    }
                    final StmtIterator isAboutStatements = getModel().listStatements(nanopublicationResource, SIO.isAbout, ontologyResource);
                    if (!isAboutStatements.hasNext()) {
                        getModel().add(nanopublicationResource, SIO.isAbout, ontologyResource);
                    }
                }
            }

            if (!getModel().listStatements(nanopublicationResource, null, (String) null).hasNext()) {
                getModel().add(nanopublicationResource, PROV.generatedAtTime, ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance())));
            }


            return new NanopublicationPart(getModel(), Uri.parse(publicationInfoResource.getURI()));
        }

        @Override
        protected final NanopublicationPartType getType() {
            return NanopublicationPartType.PUBLICATION_INFO;
        }
    }
}
