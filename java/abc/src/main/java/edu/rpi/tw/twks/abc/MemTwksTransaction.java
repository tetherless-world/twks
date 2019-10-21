package edu.rpi.tw.twks.abc;

import org.apache.jena.query.*;

final class MemTwksTransaction extends DatasetTwksTransaction {
    MemTwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return QueryExecutionFactory.create(query, getDataset().getUnionModel());
    }
}
