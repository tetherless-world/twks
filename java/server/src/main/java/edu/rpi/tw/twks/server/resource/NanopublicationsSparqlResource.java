package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import javax.ws.rs.Path;

@Path("/sparql/nanopublications")
public final class NanopublicationsSparqlResource extends AbstractSparqlResource {
    public NanopublicationsSparqlResource(final Twks twks) {
        super(twks);
    }

    public NanopublicationsSparqlResource() {
    }

    @Override
    protected final QueryExecution query(final Query query, final TwksTransaction transaction) {
        return transaction.queryNanopublications(query);
    }
}
