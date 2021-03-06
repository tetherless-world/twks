package edu.rpi.tw.twks.api;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * Assertion query interface.
 */
public interface AssertionQueryApi {
    /**
     * Query assertion parts of stored nanopublications.
     * <p>
     * See TwksTest for examples on how to use this.
     *
     * @param query query to execute. This will be augmented by the implementation as needed.
     * @return QueryExecution that is ready to execute. Must be executed within the given transaction.
     */
    QueryExecution queryAssertions(Query query);
}
