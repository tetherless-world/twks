package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;

/**
 * A Twks implementation backed by the default in-memory Dataset.
 */
public final class MemTwks extends DatasetTwks {
    public MemTwks(final TwksConfiguration configuration) {
        super(configuration, DatasetFactory.createTxnMem());
    }

    @Override
    protected final TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        return new MemTwksTransaction(getConfiguration(), getDataset(), readWrite);
    }
}
