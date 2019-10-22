package edu.rpi.tw.twks.abc;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

final class MemTwksTransaction extends DatasetTwksTransaction {
    MemTwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }
}
