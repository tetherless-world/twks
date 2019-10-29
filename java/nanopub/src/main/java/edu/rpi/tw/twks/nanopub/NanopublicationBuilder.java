package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.PROV;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Calendar;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public final class NanopublicationBuilder {
    private final NanopublicationAssertionBuilder assertionBuilder = new NanopublicationAssertionBuilder();
    // Generate a URI to use for the nanopublication and/or parts whose URIs aren't set explicitly.
    private final Uri generatedUri = Uri.parse("urn:uuid:" + UUID.randomUUID().toString());
    private final NanopublicationProvenanceBuilder provenanceBuilder = new NanopublicationProvenanceBuilder();
    private final NanopublicationPublicationInfoBuilder publicationInfoBuilder = new NanopublicationPublicationInfoBuilder();
    private @Nullable
    Uri uri = null;

    NanopublicationBuilder() {
    }

    public final Nanopublication build() {
        final Uri nanopublicationUri = this.uri != null ? this.uri : this.generatedUri;

        final NanopublicationPart assertion = assertionBuilder.build();
        final NanopublicationPart provenance = provenanceBuilder.build(assertion.getName());
        final NanopublicationPart publicationInfo = publicationInfoBuilder.build(nanopublicationUri);

        final NanopublicationPart head =
                new NanopublicationPart(
                        NanopublicationFactory.DEFAULT.createNanopublicationHead(assertion.getName(), nanopublicationUri, provenance.getName(), publicationInfo.getName()),
                        Uri.parse(generatedUri.toString() + "#head")
                );

        try {
            return NanopublicationFactory.DEFAULT.createNanopublicationFromParts(assertion, head, provenance, publicationInfo, nanopublicationUri);
        } catch (final MalformedNanopublicationException e) {
            throw new IllegalStateException(e);
        }
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

    public final NanopublicationBuilder setUri(@Nullable final Uri uri) {
        this.uri = checkNotNull(uri);
        return this;
    }

    private enum NanopublicationPartType {
        ASSERTION,
        PROVENANCE,
        PUBLICATION_INFO
    }

    public final class NanopublicationAssertionBuilder extends NanopublicationPartBuilder<NanopublicationAssertionBuilder> {
        private NanopublicationAssertionBuilder() {
        }

        private NanopublicationPart build() {
            if (!getModel().listStatements().hasNext()) {
                throw new IllegalArgumentException("no assertion statements");
            }

            return new NanopublicationPart(getModel(), getOrGenerateName());
        }

        @Override
        protected final NanopublicationPartType getType() {
            return NanopublicationPartType.ASSERTION;
        }
    }

    private abstract class NanopublicationPartBuilder<NanopublicationPartBuilderT extends NanopublicationPartBuilder<?>> {
        private Model model = ModelFactory.createDefaultModel();
        private @Nullable
        Uri name = null;

        public final Model getModel() {
            return model;
        }

        @SuppressWarnings("unchecked")
        public NanopublicationPartBuilderT setModel(final Model model) {
            this.model = checkNotNull(model);
            return (NanopublicationPartBuilderT) this;
        }

        public final NanopublicationBuilder getNanopublicationBuilder() {
            return NanopublicationBuilder.this;
        }

        protected final Uri getOrGenerateName() {
            if (this.name != null) {
                return this.name;
            }
            return Uri.parse(NanopublicationBuilder.this.generatedUri.toString() + "#" + getType().name().toLowerCase());
        }

        protected abstract NanopublicationPartType getType();

        @SuppressWarnings("unchecked")
        public final NanopublicationPartBuilderT setName(final Uri name) {
            this.name = checkNotNull(name);
            return (NanopublicationPartBuilderT) this;
        }
    }

    public final class NanopublicationProvenanceBuilder extends NanopublicationPartBuilder<NanopublicationProvenanceBuilder> {
        private NanopublicationProvenanceBuilder() {
        }

        NanopublicationPart build(final Uri assertionName) {
            final Resource assertionResource = ResourceFactory.createResource(assertionName.toString());
            if (!getModel().listStatements(assertionResource, null, (String) null).hasNext()) {
                getModel().add(assertionResource, PROV.generatedAtTime, ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance())));
            }
            return new NanopublicationPart(getModel(), getOrGenerateName());
        }

        @Override
        protected final NanopublicationPartType getType() {
            return NanopublicationPartType.PROVENANCE;
        }
    }

    public final class NanopublicationPublicationInfoBuilder extends NanopublicationPartBuilder<NanopublicationPublicationInfoBuilder> {
        private NanopublicationPublicationInfoBuilder() {
        }

        NanopublicationPart build(final Uri nanopublicationUri) {
            final Resource nanopublicationResource = ResourceFactory.createResource(nanopublicationUri.toString());
            if (!getModel().listStatements(nanopublicationResource, null, (String) null).hasNext()) {
                getModel().add(nanopublicationResource, PROV.generatedAtTime, ResourceFactory.createTypedLiteral(new XSDDateTime(Calendar.getInstance())));
            }
            return new NanopublicationPart(getModel(), getOrGenerateName());
        }

        @Override
        protected final NanopublicationPartType getType() {
            return NanopublicationPartType.PUBLICATION_INFO;
        }
    }
}
