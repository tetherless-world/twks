package edu.rpi.tw.twdb.api;

/**
 * Largely opaque interface to Twdb transactions.
 * <p>
 * Should be used with try-with-resources. For example:
 * <p>
 * try (final TwdbTransaction transaction = db.newTransaction(ReadWrite.WRITE)) {
 * transaction.commit();
 * }
 * <p>
 * Creating the TwdbTransaction automatically begins the transaction, and the close of the block ends it.
 * You should call .commit() or .abort() before exiting of the block. If you do not call one, .abort() is implied.
 *
 * @see <a href="https://jena.apache.org/documentation/txn/">transactions in Jena</a> for the semantics.
 */
public interface TwdbTransaction extends AutoCloseable {
    void abort();

    void commit();
}
