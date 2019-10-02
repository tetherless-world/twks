package edu.rpi.tw.twdb.lib;

import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.nanopub.Uris;
import edu.rpi.tw.twdb.api.Twdb;
import org.apache.jena.query.*;
import org.apache.jena.tdb2.TDB2;
import org.apache.jena.tdb2.TDB2Factory;
import org.dmfs.rfc3986.Uri;

import java.util.Optional;

public final class Tdb2Twdb implements Twdb {
    private final String getNanopublicationQueryString;
    private final Dataset tdbDataset;

    public Tdb2Twdb() {
        getNanopublicationQueryString = "prefix np: <http://www.nanopub.org/nschema#>\n" +
                "select ?G ?S ?P ?O where {\n" +
                "  {graph ?G {<%s> a np:Nanopublication}} union\n" +
                "  {graph ?H {<%s> a np:Nanopublication {<%s> np:hasAssertion ?G} union {<%s> np:hasProvenance ?G} union {<%s> np:hasPublicationInfo ?G}}}\n" +
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
    public Optional<Nanopublication> getNanopublication(final Uri uri) {
        final String uriString = Uris.toString(uri);
        final String queryString = String.format(getNanopublicationQueryString, uriString, uriString, uriString, uriString, uriString);
        final Query query = QueryFactory.create(queryString);
        tdbDataset.begin(ReadWrite.READ);
        try (final QueryExecution queryExecution = QueryExecutionFactory.create(query, tdbDataset)) {
            queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
            for (final ResultSet resultSet = queryExecution.execSelect(); resultSet.hasNext(); ) {
                final QuerySolution querySolution = resultSet.nextSolution();
                System.out.println(querySolution.toString());
//                final RDFNode x = querySolution.get("varName");       // Get a result variable by name.
//                final Resource r = querySolution.getResource("VarR"); // Get a result variable - must be a resource
//                final Literal l = querySolution.getLiteral("VarL");   // Get a result variable - must be a literal
            }
        }
        tdbDataset.end();
        return Optional.empty();
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
