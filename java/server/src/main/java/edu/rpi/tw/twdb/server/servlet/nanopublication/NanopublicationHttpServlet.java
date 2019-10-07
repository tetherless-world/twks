package edu.rpi.tw.twdb.server.servlet.nanopublication;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.rpi.tw.nanopub.Nanopublication;
import edu.rpi.tw.nanopub.Uris;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.servlet.TwdbHttpServlet;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.dmfs.rfc3986.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

@Singleton
class NanopublicationHttpServlet extends TwdbHttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(NanopublicationHttpServlet.class);

    @Inject
    public NanopublicationHttpServlet(final Twdb db) {
        super(db);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        @Nullable final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Uri nanopublicationUri = Uris.parse(pathInfo.substring(1));

        final Optional<Nanopublication> nanopublication = getDb().getNanopublication(nanopublicationUri);
        if (!nanopublication.isPresent()) {
            logger.info("nanopublication not found: {}", Uris.toString(nanopublicationUri));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Lang respLang = calculateResponseLang(Lang.TRIG, getOfferGraphAcceptList(), getProposeAcceptList(req));
        resp.setContentType(respLang.getContentType().getContentType());
        try (final OutputStream respOutputStream = resp.getOutputStream()) {
            RDFDataMgr.write(respOutputStream, nanopublication.get().toDataset(), respLang);
        }
    }
}
