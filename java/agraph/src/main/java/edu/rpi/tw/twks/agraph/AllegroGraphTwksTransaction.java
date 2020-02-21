package edu.rpi.tw.twks.agraph;

import com.franz.agraph.repository.AGRepositoryConnection;
import edu.rpi.tw.twks.abc.QuadStoreTwksTransaction;

final class AllegroGraphTwksTransaction extends QuadStoreTwksTransaction<AllegroGraphTwks> {
    public AllegroGraphTwksTransaction(final AGRepositoryConnection repositoryConnection, final AllegroGraphTwks twks) {
        super(new AllegroGraphQuadStoreTransaction(repositoryConnection), twks);
    }
}
