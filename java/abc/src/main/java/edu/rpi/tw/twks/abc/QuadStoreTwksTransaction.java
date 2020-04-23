package edu.rpi.tw.twks.abc;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.uri.Uri;
import edu.rpi.tw.twks.vocabulary.SIO;
import edu.rpi.tw.twks.vocabulary.Vocabularies;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class QuadStoreTwksTransaction<TwksT extends AbstractTwks<?>> extends AbstractTwksTransaction<TwksT> {
    private final static Logger logger = LoggerFactory.getLogger(QuadStoreTwksTransaction.class);
    private final QuadStoreTransaction quadStoreTransaction;

    protected QuadStoreTwksTransaction(final QuadStoreTransaction quadStore, final TwksT twks) {
        super(twks);
        this.quadStoreTransaction = checkNotNull(quadStore);
    }

    @Override
    public final void abort() {
        quadStoreTransaction.abort();
    }

    @Override
    public final void close() {
        quadStoreTransaction.close();
    }

    @Override
    public final void commit() {
        quadStoreTransaction.commit();
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final Set<Uri> nanopublicationGraphNames = getNanopublicationGraphNames(uri);
        if (nanopublicationGraphNames.isEmpty()) {
            return DeleteNanopublicationResult.NOT_FOUND;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        for (final Uri nanopublicationGraphName : nanopublicationGraphNames) {
            quadStoreTransaction.removeNamedGraph(nanopublicationGraphName);
        }

        {
            // Delete all assertion union graphs and rebuild from nanopublications
            // This handles the case where there are duplicates, at the cost of O(n) in the number of named graphs in the store +
            // O(m) in the number of nanopublications.
            // Delete is a rare enough operation that we don't need a better algorithm yet.
            final AllAssertionsUnionGraph allAssertionsUnionGraph = new AllAssertionsUnionGraph(quadStoreTransaction);
            final OntologyAssertionsUnionGraphs ontologyAssertionsUnionGraphs = new OntologyAssertionsUnionGraphs(quadStoreTransaction);

            allAssertionsUnionGraph.deleteNanopublication(uri);
            ontologyAssertionsUnionGraphs.deleteNanopublication(uri);

            getNanopublications(new NanopublicationConsumer() {
                @Override
                public void accept(final Nanopublication nanopublication) {
                    allAssertionsUnionGraph.putNanopublication(nanopublication);
                    ontologyAssertionsUnionGraphs.putNanopublication(nanopublication);
                }

                @Override
                public void onMalformedNanopublicationException(final MalformedNanopublicationException exception) {
                    logger.error("malformed nanopublication when rebuilding assertions union graphs:", exception);
                }
            });
        }

        return DeleteNanopublicationResult.DELETED;
    }

    @Override
    public final void deleteNanopublications() {
        quadStoreTransaction.removeAllGraphs();
    }

    @Override
    public final Model getAssertions() {
        return new AllAssertionsUnionGraph(quadStoreTransaction).get();
    }

    private ImmutableSet<Uri> getNanopublicationGraphNames(final Uri nanopublicationUri) {
        final ImmutableSet.Builder<Uri> resultBuilder = ImmutableSet.builder();
        try (final QueryExecution queryExecution = quadStoreTransaction.query(QueryFactory.create(String.format(NanopublicationQueries.GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING_TEMPLATE, nanopublicationUri)))) {
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                resultBuilder.add(Uri.parse(g.getURI()));
            }
        }
        return resultBuilder.build();
    }

    private NanopublicationPart getNanopublicationPart(final String nanopublicationPartName) {
        final Uri nanopublicationPartNameUri = Uri.parse(nanopublicationPartName);
        final Model nanopublicationPartModel;
        try {
            nanopublicationPartModel = quadStoreTransaction.getNamedGraph(nanopublicationPartNameUri);
        } catch (final NoSuchNamedGraphException e) {
            throw new IllegalStateException(e);
        }
        return new NanopublicationPart(nanopublicationPartModel, nanopublicationPartNameUri);
    }

    @Override
    protected void getNanopublications(final NanopublicationConsumer consumer) {
        try (final QueryExecution queryExecution = queryNanopublications(NanopublicationQueries.ITERATE_NANOPUBLICATIONS_QUERY)) {
            final ResultSet resultSet = queryExecution.execSelect();
            while (resultSet.hasNext()) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final String nanopublicationAssertionGraphName = querySolution.getResource("A").getURI();
                final String nanopublicationHeadGraphName = querySolution.getResource("H").getURI();
                final String nanopublicationProvenanceGraphName = querySolution.getResource("P").getURI();
                final String nanopublicationPublicationInfoGraphName = querySolution.getResource("I").getURI();
                final String nanopublicationUri = querySolution.getResource("np").getURI();

                try {
                    consumer.accept(SpecificationNanopublicationDialect.createNanopublicationFromParts(
                            getNanopublicationPart(nanopublicationAssertionGraphName),
                            Uri.parse(nanopublicationHeadGraphName),
                            Uri.parse(nanopublicationUri),
                            getNanopublicationPart(nanopublicationProvenanceGraphName),
                            getNanopublicationPart(nanopublicationPublicationInfoGraphName)
                    ));
                } catch (final MalformedNanopublicationException e) {
                    consumer.onMalformedNanopublicationException(e);
                }
            }
        }
    }

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        final Model result = ModelFactory.createDefaultModel();
        if (ontologyUris.isEmpty()) {
            return result;
        }
        Vocabularies.setNsPrefixes(result);
        final OntologyAssertionsUnionGraphs ontologyAssertionsUnionGraphs = new OntologyAssertionsUnionGraphs(quadStoreTransaction);
        for (final Uri ontologyUri : ontologyUris) {
            try {
                result.add(ontologyAssertionsUnionGraphs.get(ontologyUri));
            } catch (final NoSuchNamedGraphException e) {
            }
        }
        return result;
    }

    protected final QuadStoreTransaction getQuadStoreTransaction() {
        return quadStoreTransaction;
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri());
        switch (deleteResult) {
            case DELETED:
                logger.debug("deleted nanopublication {} before put", nanopublication.getUri());
                break;
            case NOT_FOUND:
                logger.debug("nanopublication {} did not exist before put", nanopublication.getUri());
                break;
        }

        for (final NanopublicationPart nanopublicationPart : new NanopublicationPart[]{nanopublication.getAssertion(), nanopublication.getHead(), nanopublication.getProvenance(), nanopublication.getPublicationInfo()}) {
            if (quadStoreTransaction.containsNamedGraph(nanopublicationPart.getName())) {
                throw new DuplicateNanopublicationPartName(nanopublicationPart.getName().toString());
            }
            quadStoreTransaction.addNamedGraph(nanopublicationPart.getName(), nanopublicationPart.getModel());
        }

        new AllAssertionsUnionGraph(quadStoreTransaction).putNanopublication(nanopublication);
        new OntologyAssertionsUnionGraphs(quadStoreTransaction).putNanopublication(nanopublication);

        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public final QueryExecution queryAssertions(final Query query) {
        return new AllAssertionsUnionGraph(quadStoreTransaction).query(query);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return quadStoreTransaction.query(query);
    }

    private final static class AllAssertionsUnionGraph {
        private final static Uri NAME = Uri.parse("urn:twks:assertions:all");
        private final QuadStoreTransaction quadStoreTransaction;

        public AllAssertionsUnionGraph(final QuadStoreTransaction quadStoreTransaction) {
            this.quadStoreTransaction = checkNotNull(quadStoreTransaction);
        }

        public final void deleteNanopublication(final Uri nanopublicationUri) {
            quadStoreTransaction.removeNamedGraph(NAME);
        }

        public final Model get() {
            try {
                return quadStoreTransaction.getNamedGraph(NAME);
            } catch (final NoSuchNamedGraphException e) {
                logger.warn("requested all assertions graph, which hasn't been created yet");
                return ModelFactory.createDefaultModel();
            }
        }

        public final void putNanopublication(final Nanopublication nanopublication) {
            quadStoreTransaction.getOrCreateNamedGraph(NAME).add(nanopublication.getAssertion().getModel());
        }

        public final QueryExecution query(final Query query) {
            query.addGraphURI(NAME.toString());
            return quadStoreTransaction.query(query);
        }
    }

    private final static class OntologyAssertionsUnionGraphs {
        private final static Uri INDEX_GRAPH_NAME = Uri.parse("urn:twks:assertions:ontology");
        private final static String UNION_GRAPH_NAME_PREFIX = "urn:twks:assertions:ontology:";
        private final QuadStoreTransaction quadStoreTransaction;

        public OntologyAssertionsUnionGraphs(final QuadStoreTransaction quadStoreTransaction) {
            this.quadStoreTransaction = checkNotNull(quadStoreTransaction);
        }

        private Uri buildUnionGraphName(final Uri ontologyUri) {
            try {
                return Uri.parse(UNION_GRAPH_NAME_PREFIX + URLEncoder.encode(ontologyUri.toString(), Charsets.UTF_8.name()));
            } catch (final UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }

        public final void deleteNanopublication(final Uri nanopublicationUri) {
            final Model index;
            try {
                index = checkNotNull(quadStoreTransaction.getNamedGraph(INDEX_GRAPH_NAME));
            } catch (final NoSuchNamedGraphException e) {
                return;
            }

            final Resource nanopublicationResource = ResourceFactory.createResource(nanopublicationUri.toString());

            final List<Uri> unionGraphNames = new ArrayList<>();
            index.listObjectsOfProperty(nanopublicationResource, SIO.isAbout).forEachRemaining(object -> {
                if (!object.isURIResource()) {
                    return;
                }
                final Resource resource = object.asResource();
                if (!resource.getURI().startsWith(UNION_GRAPH_NAME_PREFIX)) {
                    return;
                }
                unionGraphNames.add(Uri.parse(resource.getURI()));
            });

            index.removeAll(nanopublicationResource, SIO.isAbout, null);

            for (final Uri unionGraphName : unionGraphNames) {
                quadStoreTransaction.removeNamedGraph(unionGraphName);
            }
        }

        public final Model get(final Uri ontologyUri) throws NoSuchNamedGraphException {
            return quadStoreTransaction.getNamedGraph(buildUnionGraphName(ontologyUri));
        }

        public final void putNanopublication(final Nanopublication nanopublication) {
            // For each ontology the nanopublication "is about", add the nanopublication's assertions to that ontology's assertions union
            // We keep track of these per-ontology assertions union graphs with a separate "index" graph with statements of the form
            // <nanopublication URI> sio:isAbout <union graph URI>
            final Model index = quadStoreTransaction.getOrCreateNamedGraph(INDEX_GRAPH_NAME);
//            System.out.println("Index: ");
//            index.listStatements().forEachRemaining(statement -> System.out.println(statement));
//            System.out.println();

            for (final Uri ontologyUri : nanopublication.getAboutOntologyUris()) {
                final Uri unionGraphName = buildUnionGraphName(ontologyUri);
                final Model unionGraph = quadStoreTransaction.getOrCreateNamedGraph(unionGraphName);
                unionGraph.add(nanopublication.getAssertion().getModel());
                index.add(ResourceFactory.createResource(nanopublication.getUri().toString()), SIO.isAbout, ResourceFactory.createResource(unionGraphName.toString()));
//                System.out.println(ontologyUri + " union: ");
//                unionGraph.listStatements().forEachRemaining(statement -> System.out.println(statement));
//                System.out.println();
            }

//            System.out.println("Index: ");
//            index.listStatements().forEachRemaining(statement -> System.out.println(statement));
//            System.out.println();
        }
    }
}
