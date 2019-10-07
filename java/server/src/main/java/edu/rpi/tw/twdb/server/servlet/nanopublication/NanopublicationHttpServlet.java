package edu.rpi.tw.twdb.server.servlet.nanopublication;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.rpi.tw.nanopub.*;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.servlet.TwdbHttpServlet;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.dmfs.rfc3986.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
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
        final Optional<Uri> nanopublicationUri = getNanopublicationUri(req);
        if (!nanopublicationUri.isPresent()) {
            logger.info("no nanopublication URI specified in request");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Optional<Nanopublication> nanopublication = getDb().getNanopublication(nanopublicationUri.get());
        if (!nanopublication.isPresent()) {
            logger.info("nanopublication not found: {}", Uris.toString(nanopublicationUri.get()));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final Lang respLang = calculateResponseLang(Lang.TRIG, getOfferGraphAcceptList(), getProposeAcceptList(req));
        resp.setContentType(respLang.getContentType().getContentType());
        try (final OutputStream respOutputStream = resp.getOutputStream()) {
            RDFDataMgr.write(respOutputStream, nanopublication.get().toDataset(), respLang);
        }
    }

    private Optional<Uri> getNanopublicationUri(final HttpServletRequest req) {
        @Nullable final String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() < 2) {
            return Optional.empty();
        }

        return Optional.of(Uris.parse(pathInfo.substring(1)));
    }

    private Lang getRequestLang(final HttpServletRequest req) throws IOException {
        @Nullable final String contentType = req.getContentType();
        if (contentType == null || contentType.isEmpty()) {
            throw new IllegalArgumentException("missing Content-Type");
        }

        @Nullable final Lang reqLang = RDFLanguages.contentTypeToLang(contentType);
        if (reqLang == null) {
            logger.error("PUT request has a non-RDF Content-Type: {}", contentType);
            throw new IllegalArgumentException(contentType);
        }

        return reqLang;
    }

    private Optional<NanopublicationDialect> getRequestNanopublicationDialect(final HttpServletRequest req) {
        final @Nullable String dialectString = req.getHeader("X-Nanopublication-Dialect");
        if (dialectString == null) {
            return Optional.empty();
        }

        return Optional.of(NanopublicationDialect.valueOf(dialectString.toUpperCase()));
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final Nanopublication nanopublication;
        try {
            nanopublication = parseRequestNanopublication(req, resp);
        } catch (final IllegalArgumentException | MalformedNanopublicationException e) {
            logger.error("error parsing nanopublication from PUT request: ", e);
            return;
        }

        getDb().putNanopublication(nanopublication);

        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    private Nanopublication parseRequestNanopublication(final HttpServletRequest req, final HttpServletResponse resp) throws MalformedNanopublicationException, IOException {
        final Lang reqLang = getRequestLang(req);

        final NanopublicationParser parser = new NanopublicationParser();
        parser.setLang(reqLang);

        final Optional<NanopublicationDialect> dialect = getRequestNanopublicationDialect(req);
        if (dialect.isPresent()) {
            parser.setDialect(dialect.get());
        }

        final Optional<Uri> nanopublicationUri = getNanopublicationUri(req);

        final String reqBody;
        try (final Reader reqReader = req.getReader()) {
            reqBody = CharStreams.toString(req.getReader());
        }

        return parser.parse(new StringReader(reqBody), nanopublicationUri);
    }
}
