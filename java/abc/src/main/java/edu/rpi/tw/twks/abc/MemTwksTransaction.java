package edu.rpi.tw.twks.abc;

import org.apache.jena.query.*;

final class MemTwksTransaction extends DatasetTwksTransaction {
    MemTwksTransaction(final Dataset dataset, final ReadWrite readWrite) {
        super(dataset, readWrite);
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        final Dataset datasetCopy = DatasetFactory.create(getDataset());
        // Only way to emulate TDB2.symUnionDefaultGraph semantics
        datasetCopy.setDefaultModel(datasetCopy.getUnionModel());
        return QueryExecutionFactory.create(query, datasetCopy);
    }
}
