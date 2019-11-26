package edu.rpi.tw.twks.abc;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

final class MemTwksTransaction extends DatasetTwksTransaction<MemTwks> {
    MemTwksTransaction(final Dataset dataset, final ReadWrite readWrite, final MemTwks twks) {
        super(dataset, readWrite, twks);
    }
}
