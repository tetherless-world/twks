package edu.rpi.tw.twks.tdb;

import edu.rpi.tw.twks.abc.DatasetTwksTransaction;
import org.apache.jena.query.*;
import org.apache.jena.tdb2.TDB2;

final class Tdb2TwksTransaction extends DatasetTwksTransaction {
    Tdb2TwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        final QueryExecution queryExecution = QueryExecutionFactory.create(query, getDataset());
        queryExecution.getContext().set(TDB2.symUnionDefaultGraph, true);
        return queryExecution;
    }
}
