package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.abc.DatasetTwks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb2.TDB2Factory;

public final class Tdb2Twks extends DatasetTwks<Tdb2TwksConfiguration> {
    public Tdb2Twks(final Tdb2TwksConfiguration configuration) {
        super(configuration, TDB2Factory.connectDataset(configuration.getLocation().isPresent() ? Location.create(configuration.getLocation().get()) : Location.mem()));
    }

    @Override
    protected final TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        return new Tdb2TwksTransaction(readWrite, this);
    }
}
