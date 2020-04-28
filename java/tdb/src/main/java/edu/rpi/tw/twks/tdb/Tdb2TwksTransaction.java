package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.abc.DatasetTwksTransaction;
import edu.rpi.tw.twks.abc.QuadStoreTwksMetrics;
import org.apache.jena.query.ReadWrite;

final class Tdb2TwksTransaction extends DatasetTwksTransaction<Tdb2Twks, Tdb2TwksConfiguration, QuadStoreTwksMetrics> {
    Tdb2TwksTransaction(final ReadWrite readWrite, final Tdb2Twks twks) {
        super(readWrite, twks);
    }
}
