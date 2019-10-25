package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksConfiguration;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;

final class MemTwksTransaction extends DatasetTwksTransaction {
    MemTwksTransaction(final TwksConfiguration configuration, final Dataset dataset, final ReadWrite readWrite) {
        super(configuration, dataset, readWrite);
    }
}
