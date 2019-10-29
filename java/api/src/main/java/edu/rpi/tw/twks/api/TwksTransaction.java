package edu.rpi.tw.twks.api;

/**
 * High-level interface to Twks transactions.
 * <p>
 * Implements the public-facing APIs, whose operations can only be invoked within a transaction.
 * <p>
 * Should be used with try-with-resources. For example:
 * <code>
 * try (final TwksTransaction transaction = twks.newTransaction(ReadWrite.WRITE)) {
 * transaction.commit();
 * }
 * </code>
 * Creating the TwksTransaction automatically begins the transaction, and the close of the block ends it.
 * You should call .commit() or .abort() before exiting of the block. If you do not call one, .abort() is implied.
 *
 * @see <a href="https://jena.apache.org/documentation/txn/">transactions in Jena</a> for the semantics.
 */
public interface TwksTransaction extends AdministrationApi, AutoCloseable, GetAssertionsApi, NanopublicationCrudApi, QueryApi {
    /**
     * Abort the transaction.
     */
    void abort();

    /**
     * Close/end the transaction.
     * <p>
     * Overriden because it only throws RuntimeExceptions.
     */
    @Override
    void close();

    /**
     * Commit the transaction.
     */
    void commit();
}
