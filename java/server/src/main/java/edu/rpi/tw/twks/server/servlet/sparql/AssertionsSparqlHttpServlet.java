package edu.rpi.tw.twks.server.servlet.sparql;

import edu.rpi.tw.twks.lib.Twks;
import edu.rpi.tw.twks.lib.TwksTransaction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public final class AssertionsSparqlHttpServlet extends SparqlHttpServlet {
    public AssertionsSparqlHttpServlet() {
        super();
    }

    public AssertionsSparqlHttpServlet(final Twks db) {
        super(db);
    }

    @Override
    protected final QueryExecution query(final Query query, final TwksTransaction transaction) {
        return getDb().queryAssertions(query, transaction);
    }
}
