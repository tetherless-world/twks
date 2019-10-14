package edu.rpi.tw.twks.server.servlet.sparql;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public final class AssertionsSparqlHttpServlet extends SparqlHttpServlet {
    public AssertionsSparqlHttpServlet() {
        super();
    }

    public AssertionsSparqlHttpServlet(final Twks twks) {
        super(twks);
    }

    @Override
    protected final QueryExecution query(final Query query, final TwksTransaction transaction) {
        return transaction.queryAssertions(query);
    }
}
