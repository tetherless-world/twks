package edu.rpi.tw.twks.agraph;

import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import edu.rpi.tw.twks.abc.QuadStoreTwks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.ReadWrite;

public final class AllegroGraphTwks extends QuadStoreTwks<AllegroGraphTwksConfiguration> {
    private @Nullable
    AGCatalog catalog = null;
    private @Nullable
    AGRepository repository = null;
    private @Nullable
    AGServer server = null;

    public AllegroGraphTwks(final AllegroGraphTwksConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        final AGRepositoryConnection connection = getRepository().getConnection();
        return new AllegroGraphTwksTransaction(connection, this);
    }

    private final synchronized AGCatalog getCatalog() {
        if (catalog == null) {
            catalog = getServer().createCatalog(getConfiguration().getCatalogId());
        }
        return catalog;
    }

    private final synchronized AGRepository getRepository() {
        if (repository == null) {
            repository = getCatalog().createRepository(getConfiguration().getRepositoryId());
            repository.initialize();
        }
        return repository;
    }

    private final synchronized AGServer getServer() {
        if (server == null) {
            server = new AGServer(getConfiguration().getServerUrl(), getConfiguration().getUsername(), getConfiguration().getPassword());
        }
        return server;
    }
}
