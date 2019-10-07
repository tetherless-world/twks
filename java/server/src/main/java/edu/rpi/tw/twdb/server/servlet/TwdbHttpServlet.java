package edu.rpi.tw.twdb.server.servlet;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.ServletContextTwdb;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaRange;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class TwdbHttpServlet extends HttpServlet {
    private final AcceptList offerGraphAcceptList;
    private final Twdb db;

    protected TwdbHttpServlet() {
        this(ServletContextTwdb.getInstance());
    }

    protected TwdbHttpServlet(final Twdb db) {
        this.db = checkNotNull(db);
        offerGraphAcceptList = toAcceptList(Lang.RDFXML, Lang.NTRIPLES, Lang.NT, Lang.N3, Lang.TURTLE, Lang.TTL, Lang.JSONLD, Lang.RDFJSON, Lang.NQUADS, Lang.NQ, Lang.TRIG, Lang.TRIG);
    }

    protected static AcceptList toAcceptList(final Lang... languages) {
        final List<MediaRange> mediaRanges = new ArrayList<>();
        for (final Lang lang : languages) {
            final String contentType = lang.getContentType().getContentType();
            mediaRanges.add(new MediaRange(contentType));
        }
        return new AcceptList(mediaRanges);
    }

    protected final Lang calculateResponseLang(final Lang defaultResponseLang, final AcceptList offerAcceptList, final Optional<AcceptList> proposeAcceptList) {
        if (!proposeAcceptList.isPresent()) {
            return defaultResponseLang;
        }
        final MediaType respMediaType = AcceptList.match(proposeAcceptList.get(), offerAcceptList);
        if (respMediaType == null) {
            return defaultResponseLang;
        }
        final Lang respLang = RDFLanguages.contentTypeToLang(respMediaType.getContentType());
        if (respLang != null) {
            return respLang;
        } else {
            return defaultResponseLang;
        }
    }

    protected final Optional<AcceptList> getProposeAcceptList(final HttpServletRequest req) {
        final String acceptHeader = req.getHeader("Accept");
        if (acceptHeader == null) {
            return Optional.empty();
        }
        return Optional.of(new AcceptList(acceptHeader));
    }

    protected final AcceptList getOfferGraphAcceptList() {
        return offerGraphAcceptList;
    }

    protected final Twdb getDb() {
        return db;
    }
}
