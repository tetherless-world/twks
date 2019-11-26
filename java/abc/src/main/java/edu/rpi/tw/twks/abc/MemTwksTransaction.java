package edu.rpi.tw.twks.abc;

import org.apache.jena.query.ReadWrite;

final class MemTwksTransaction extends DatasetTwksTransaction<MemTwks> {
    MemTwksTransaction(final ReadWrite readWrite, final MemTwks twks) {
        super(readWrite, twks);
    }
}
