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

import java.util.Optional;

public final class Tdb2Twdb implements Twdb {
    private final String getNanopublicationQueryString;
    private final Dataset tdbDataset;

    public Tdb2Twdb() {
        // http://nanopub.org/guidelines/working_draft/
        getNanopublicationQueryString = "prefix np: <http://www.nanopub.org/nschema#>\n" +
                "prefix : <%s>\n" +
                "select ?G ?S ?P ?O where {\n" +
                "  {graph ?G {: a np:Nanopublication}} union\n" +
                "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
                "  graph ?G {?S ?P ?O}\n" +
                "}";
        this.tdbDataset = TDB2Factory.createDataset();
    }

    @Override
    public boolean deleteNanopublication(final Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dataset getAssertionsDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Nanopublication> getNanopublication(final Uri uri) throws MalformedNanopublicationException {
        final String uriString = Uris.toString(uri);
        final String queryString = String.format(getNanopublicationQueryString, uriString);
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
        if (!nanopublicationDataset.isEmpty()) {
            return Optional.of(NanopublicationFactory.getInstance().createNanopublicationFromDataset(nanopublicationDataset));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Dataset getNanopublicationsDataset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putNanopublication(final Nanopublication nanopublication) {
        tdbDataset.begin(ReadWrite.WRITE);
        nanopublication.toDataset(tdbDataset);
        tdbDataset.commit();
        tdbDataset.end();
    }
}
