package edu.rpi.tw.twks.server.servlet;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AcceptLists;
import edu.rpi.tw.twks.server.ServletContextTwks;
import org.apache.jena.atlas.web.AcceptList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwksHttpServlet extends HttpServlet {
    private final Twks db;

    protected TwksHttpServlet() {
        this(ServletContextTwks.getInstance());
    }

    protected TwksHttpServlet(final Twks db) {
        this.db = checkNotNull(db);
    }

    public final static Optional<AcceptList> getProposeAcceptList(final HttpServletRequest req) {
        return AcceptLists.getProposeAcceptList(req.getHeader("Accept"));
    }

    protected final Twks getDb() {
        return db;
    }
}
