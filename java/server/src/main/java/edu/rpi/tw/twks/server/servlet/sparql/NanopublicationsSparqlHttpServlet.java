package edu.rpi.tw.twks.server.servlet.sparql;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public final class NanopublicationsSparqlHttpServlet extends SparqlHttpServlet {
    public NanopublicationsSparqlHttpServlet() {
        super();
    }

    public NanopublicationsSparqlHttpServlet(final Twks twks) {
        super(twks);
    }

    @Override
    protected final QueryExecution query(final Query query, final TwksTransaction transaction) {
        return transaction.queryNanopublications(query);
    }
}
