package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.abc.AbstractTwks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb2.TDB2Factory;

public final class Tdb2Twks extends AbstractTwks {
    private final Dataset tdbDataset;

    public Tdb2Twks() {
        this(Location.mem());
    }

    public Tdb2Twks(final Location location) {
        this.tdbDataset = TDB2Factory.connectDataset(location);
    }

    @Override
    protected final TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        return new Tdb2TwksTransaction(tdbDataset, readWrite);
    }
}
