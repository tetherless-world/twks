package edu.rpi.tw.twks.nanopub;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public final class NanopublicationQueries {
    //    private final static String GET_NANOPUBLICATION_DATASET_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
//            "prefix : <%s>\n" +
//            "select ?G ?S ?P ?O where {\n" +
//            "  {graph ?G {: a np:Nanopublication}} union\n" +
//            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
//            "  graph ?G {?S ?P ?O}\n" +
//            "}";
    public final static String GET_NANOPUBLICATION_GRAPH_NAMES_QUERY_STRING_TEMPLATE = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "prefix : <%s>\n" +
            "select ?G where {\n" +
            "  {graph ?G {: a np:Nanopublication}} union\n" +
            "  {graph ?H {: a np:Nanopublication {: np:hasAssertion ?G} union {: np:hasProvenance ?G} union {: np:hasPublicationInfo ?G}}}\n" +
            "  graph ?G {?S ?P ?O}\n" +
            "}";
    public final static String ITERATE_NANOPUBLICATIONS_QUERY_STRING = "prefix np: <http://www.nanopub.org/nschema#>\n" +
            "select ?A ?H ?I ?np ?P where {\n" +
            "graph ?H {\n" +
            "  ?np a np:Nanopublication .\n" +
            "  ?np np:hasAssertion ?A .\n" +
            "  ?np np:hasProvenance ?P .\n" +
            "  ?np np:hasPublicationInfo ?I .\n" +
            "}}";
    public final static Query ITERATE_NANOPUBLICATIONS_QUERY = QueryFactory.create(ITERATE_NANOPUBLICATIONS_QUERY_STRING);

    private NanopublicationQueries() {
    }
}
