package edu.rpi.tw.twks.agraph;

import com.franz.agraph.repository.AGRepositoryConnection;
import edu.rpi.tw.twks.abc.QuadStoreTwksTransaction;

import static com.google.common.base.Preconditions.checkNotNull;

final class AllegroGraphTwksTransaction extends QuadStoreTwksTransaction<AllegroGraphTwks> {
    private final AGRepositoryConnection repositoryConnection;

    public AllegroGraphTwksTransaction(final AGRepositoryConnection repositoryConnection, final AllegroGraphTwks twks) {
        super(new AllegroGraphQuadStoreTransaction(repositoryConnection), twks);
        this.repositoryConnection = checkNotNull(repositoryConnection);
        repositoryConnection.begin();
    }

    @Override
    protected final AllegroGraphQuadStoreTransaction getQuadStoreTransaction() {
        return (AllegroGraphQuadStoreTransaction) super.getQuadStoreTransaction();
    }
}
