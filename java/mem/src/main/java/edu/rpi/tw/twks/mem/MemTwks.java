package edu.rpi.tw.twks.mem;

import edu.rpi.tw.twks.abc.DatasetTwks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;

/**
 * A Twks implementation backed by the default in-memory Dataset.
 */
public final class MemTwks extends DatasetTwks<MemTwksConfiguration> {
    public MemTwks() {
        this(MemTwksConfiguration.builder().build());
    }

    public MemTwks(final MemTwksConfiguration configuration) {
        super(configuration, DatasetFactory.createTxnMem());
    }

    @Override
    protected final TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        return new MemTwksTransaction(readWrite, this);
    }
}
