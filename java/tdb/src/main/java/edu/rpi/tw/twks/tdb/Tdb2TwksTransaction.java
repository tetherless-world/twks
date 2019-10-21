package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.abc.DatasetTwksTransaction;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.tdb2.TDB2;

final class Tdb2TwksTransaction extends DatasetTwksTransaction {
    Tdb2TwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }

    @Override
    protected void setUnionDefaultGraph(final Context context) {
        context.set(TDB2.symUnionDefaultGraph, true);
    }
}
