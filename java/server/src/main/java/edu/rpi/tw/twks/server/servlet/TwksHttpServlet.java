package edu.rpi.tw.twks.server.servlet;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AcceptLists;
import edu.rpi.tw.twks.server.ServletTwks;
import org.apache.jena.atlas.web.AcceptList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwksHttpServlet extends HttpServlet {
    private final Twks twks;

    protected TwksHttpServlet() {
        this(ServletTwks.getInstance().getTwks());
    }

    protected TwksHttpServlet(final Twks twks) {
        this.twks = checkNotNull(twks);
    }

    public final static Optional<AcceptList> getProposeAcceptList(final HttpServletRequest req) {
        return AcceptLists.getProposeAcceptList(req.getHeader("Accept"));
    }

    protected final Twks getTwks() {
        return twks;
    }
}
