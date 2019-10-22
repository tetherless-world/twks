package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.abc.DatasetTwksTransaction;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

final class Tdb2TwksTransaction extends DatasetTwksTransaction {
    Tdb2TwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }
}
