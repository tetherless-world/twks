package edu.rpi.tw.twks.mem;

import edu.rpi.tw.twks.abc.DatasetTwksTransaction;
import edu.rpi.tw.twks.abc.QuadStoreTwksMetrics;
import org.apache.jena.query.ReadWrite;

final class MemTwksTransaction extends DatasetTwksTransaction<MemTwks, MemTwksConfiguration, QuadStoreTwksMetrics> {
    MemTwksTransaction(final ReadWrite readWrite, final MemTwks twks) {
        super(readWrite, twks);
    }
}
