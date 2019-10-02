package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.nanopub.NanopublicationFactory;
import edu.rpi.tw.nanopub.Uris;
import edu.rpi.tw.nanopub.vocabulary.Vocabularies;
import edu.rpi.tw.twdb.api.Twdb;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.dmfs.rfc3986.Uri;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class Tdb2Twdb implements Twdb {
    // http://nanopub.org/guidelines/working_draft/
    private final static String GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";

    private final static String GET_NANOPUBLICATION_DATASET_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G ?S ?P ?O where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";

    private final Dataset tdbDataset;

    public Tdb2Twdb() {
        this.tdbDataset = TDB2Factory.createDataset();
    }

    @Override
    public final boolean deleteNanopublication(final Uri uri) {
        tdbDataset.begin(ReadWrite.WRITE);
        final boolean result = deleteNanopublicationInTransaction(uri);
        if (result) {
            tdbDataset.commit();
        } else {
            tdbDataset.abort();
        }
        tdbDataset.end();
        return result;
    }

    private boolean deleteNanopublicationInTransaction(final Uri uri) {
        final Set<String> nanopublicationGraphNames = getNanopublicationGraphNamesInTransaction(uri);
        if (nanopublicationGraphNames.isEmpty()) {
            return false;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        for (final String nanopublicationGraphName : nanopublicationGraphNames) {
            tdbDataset.removeNamedModel(nanopublicationGraphName);
        }
        return true;
    }

    @Override
    public final Dataset getAssertionsDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Optional<Nanopublication> getNanopublication(final Uri uri) throws MalformedNanopublicationException {
        final Dataset nanopublicationDataset = getNanopublicationDataset(uri);
        if (!nanopublicationDataset.isEmpty()) {
            return Optional.of(NanopublicationFactory.getInstance().createNanopublicationFromDataset(nanopublicationDataset));
        } else {
            return Optional.empty();
        }
    }

    private Dataset getNanopublicationDataset(final Uri uri) {
        final String uriString = Uris.toString(uri);
        final String queryString = String.format(GET_NANOPUBLICATION_DATASET_QUERY_STRING, uriString);
        final Query query = QueryFactory.create(queryString);
        tdbDataset.begin(ReadWrite.READ);
        final Dataset nanopublicationDataset = DatasetFactory.create();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset)) {
            queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                final RDFNode o = querySolution.get("O");
                final Property p = ResourceFactory.createProperty(querySolution.getResource("P").getURI());
                final Resource s = querySolution.getResource("S");

                Model model = nanopublicationDataset.getNamedModel(g.getURI());
                if (model == null) {
                    model = ModelFactory.createDefaultModel();
                    Vocabularies.setNsPrefixes(model);
                    nanopublicationDataset.addNamedModel(g.getURI(), model);
                }
                model.add(s, p, o);
            }
        }
        tdbDataset.end();
        return nanopublicationDataset;
    }

    private Set<String> getNanopublicationGraphNamesInTransaction(final Uri uri) {
        final String uriString = Uris.toString(uri);
        final String queryString = String.format(GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING, uriString);
        final Query query = QueryFactory.create(queryString);
        final Set<String> nanopublicationGraphNames = new HashSet<>();
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset)) {
            queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                final Resource g = querySolution.getResource("G");
                nanopublicationGraphNames.add(g.getURI());
            }
        }
        return nanopublicationGraphNames;
    }

    @Override
    public final Dataset getNanopublicationsDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void putNanopublication(final Nanopublication nanopublication) {
        tdbDataset.begin(ReadWrite.WRITE);
        deleteNanopublicationInTransaction(nanopublication.getUri());
        nanopublication.toDataset(tdbDataset);
        tdbDataset.commit();
        tdbDataset.end();
    }
}
