package edu.rpi.tw.twks.agraph;

import com.franz.agraph.repository.AGRepositoryConnection;
import edu.rpi.tw.twks.abc.QuadStoreTwksMetrics;
import edu.rpi.tw.twks.abc.QuadStoreTwksTransaction;

final class AllegroGraphTwksTransaction extends QuadStoreTwksTransaction<AllegroGraphTwks, AllegroGraphTwksConfiguration, QuadStoreTwksMetrics> {
    public AllegroGraphTwksTransaction(final AGRepositoryConnection repositoryConnection, final AllegroGraphTwks twks) {
        super(new AllegroGraphQuadStoreTransaction(repositoryConnection), twks);
    }
}
