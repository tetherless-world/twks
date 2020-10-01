package edu.rpi.tw.twks.tdb;

import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb2.TDB2Factory;

public final class Tdb2DatasetFactory {
    private final static Tdb2DatasetFactory instance = new Tdb2DatasetFactory();

    public final static Tdb2DatasetFactory getInstance() {
        return instance;
    }

    public final Dataset createTdb2Dataset(final Tdb2TwksConfiguration configuration) {
        return TDB2Factory.connectDataset(
                configuration.getLocation().orElse("mem").equalsIgnoreCase("mem") ?
                        Location.mem() :
                        Location.create(configuration.getLocation().get()));
    }
}
