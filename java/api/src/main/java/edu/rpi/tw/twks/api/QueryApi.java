package edu.rpi.tw.twks.api;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

/**
 * Query interface.
 */
public interface QueryApi {
    /**
     * Get a Model that contains the union of all assertion parts of stored nanopublications.
     * <p>
     * Writes to the returned Model will not be reflected in the underlying store.
     * <p>
     * The combining rule for assertions from different nanopublications is currently (201910) unspecified.
     */
    Model getAssertions();

    /**
     * Query assertion parts of stored nanopublications.
     * <p>
     * See TwksTest for examples on how to use this.
     *
     * @param query query to execute. This will be augmented by the implementation as needed.
     * @return QueryExecution that is ready to execute. Must be executed within the given transaction.
     */
    QueryExecution queryAssertions(Query query);

    /**
     * Query all parts of stored nanopublications (head, assertion, provenance, publication info).
     *
     * @param query query to execute. This will be augmented by the implementation as needed.
     * @return QueryExecution that is ready to execute. Must be executed within the given transaction.
     */
    QueryExecution queryNanopublications(Query query);
}
