package edu.rpi.tw.twdb.server.servlet.sparql;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

public final class NanopublicationsSparqlHttpServlet extends SparqlHttpServlet {
    public NanopublicationsSparqlHttpServlet() {
        super();
    }

    public NanopublicationsSparqlHttpServlet(final Twdb db) {
        super(db);
    }

    @Override
    protected final QueryExecution query(final Query query, final TwdbTransaction transaction) {
        return getDb().queryNanopublications(query, transaction);
    }
}
