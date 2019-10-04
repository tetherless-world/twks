package edu.rpi.tw.twdb.server.servlet.sparql;

import edu.rpi.tw.nanopub.Uris;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.api.TwdbTransaction;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaRange;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.dmfs.rfc3986.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract class SparqlHttpServlet extends HttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(SparqlHttpServlet.class);
    private final Twdb db;
    private final AcceptList offerGraphAcceptList;
    private final AcceptList offerResultsAcceptList;

    protected SparqlHttpServlet(final Twdb db) {
        this.db = db;

        offerGraphAcceptList = toAcceptList(Lang.RDFXML, Lang.NTRIPLES, Lang.NT, Lang.N3, Lang.TURTLE, Lang.TTL, Lang.JSONLD, Lang.RDFJSON, Lang.NQUADS, Lang.NQ, Lang.TRIG, Lang.TRIG);
        offerResultsAcceptList = toAcceptList(ResultSetLang.SPARQLResultSetCSV, ResultSetLang.SPARQLResultSetJSON, ResultSetLang.SPARQLResultSetTSV, ResultSetLang.SPARQLResultSetXML);
    }

    private static AcceptList toAcceptList(final Lang... languages) {
        final List<MediaRange> mediaRanges = new ArrayList<>();
        for (final Lang lang : languages) {
            mediaRanges.add(new MediaRange(lang.getContentType().toString()));
        }
        return new AcceptList(mediaRanges);
    }

    private static List<Uri> parseUriList(final String[] uriStrings) {
        if (uriStrings == null || uriStrings.length == 0) {
            return Collections.emptyList();
        }
        final List<Uri> uris = new ArrayList<>(uriStrings.length);
        for (final String uriString : uriStrings) {
            final Uri uri = Uris.parse(uriString);
            uri.fragment(); // Force parse
            uris.add(uri);
        }
        return uris;
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().equalsIgnoreCase("application/sparql-query")) {
            doGetPostWithParameters(req, resp);
            return;
        }

        // POST directly, query is the body
        final String queryString = IOUtils.toString(req.getReader());

        service(Collections.emptyList(), Collections.emptyList(), queryString, req, resp);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        doGetPostWithParameters(req, resp);
    }

    private void doGetPostWithParameters(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final String queryString = req.getParameter("query");
        if (queryString == null) {
            logger.error("missing query");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final List<Uri> defaultGraphUris = parseUriList(req.getParameterValues("default-graph-uri"));
        final List<Uri> namedGraphUri = parseUriList(req.getParameterValues("named-graph-uri"));

        service(defaultGraphUris, namedGraphUri, queryString, req, resp);
    }

    private void service(final List<Uri> defaultGraphUris, final List<Uri> namedGraphUris, final String queryString, final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        final Query query;
        try {
            query = QueryFactory.create(queryString);
        } catch (final QueryException e) {
            logger.error("error parsing query '{}': ", queryString, e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        for (final Uri defaultGraphUri : defaultGraphUris) {
            query.addGraphURI(Uris.toString(defaultGraphUri));
        }
        for (final Uri namedGraphUri : namedGraphUris) {
            query.addNamedGraphURI(Uris.toString(namedGraphUri));
        }

        AcceptList proposeAcceptList = null;
        {
            final String acceptHeader = req.getHeader("Accept");
            if (acceptHeader != null) {
                proposeAcceptList = new AcceptList(acceptHeader);
            }
        }

        try (final TwdbTransaction transaction = db.beginTransaction(ReadWrite.READ)) {
            try (final QueryExecution queryExecution = this.query(query, transaction)) {
                switch (query.getQueryType()) {
                    case Query.QueryTypeAsk:
                    case Query.QueryTypeSelect: {
                        final Lang respLang;
                        if (proposeAcceptList != null) {
                            final MediaType respMediaType = AcceptList.match(proposeAcceptList, offerResultsAcceptList);
                            respLang = RDFLanguages.contentTypeToLang(respMediaType.getContentType());
                        } else {
                            respLang = ResultSetLang.SPARQLResultSetXML;
                        }

                        try (final OutputStream respOutputStream = resp.getOutputStream()) {
                            if (query.getQueryType() == Query.QueryTypeAsk) {
                                final boolean result = queryExecution.execAsk();
                                ResultSetFormatter.output(respOutputStream, result, respLang);
                            } else {
                                final ResultSet resultSet = queryExecution.execSelect();
                                ResultSetFormatter.output(respOutputStream, resultSet, respLang);
                            }
                        }
                        break;
                    }
                    case Query.QueryTypeConstruct:
                    case Query.QueryTypeDescribe: {
                        final Lang respLang;
                        if (proposeAcceptList != null) {
                            final MediaType respMediaType = AcceptList.match(proposeAcceptList, offerGraphAcceptList);
                            respLang = RDFLanguages.contentTypeToLang(respMediaType.getContentType());
                        } else {
                            respLang = Lang.TRIG;
                        }

                        final Model respModel = query.getQueryType() == Query.QueryTypeConstruct ? queryExecution.execConstruct() : queryExecution.execDescribe();
                        try (final OutputStream respOutputStream = resp.getOutputStream()) {
                            respModel.write(respOutputStream, respLang.getName());
                        }
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("" + query.getQueryType());
                }
            }
        }
    }

    protected abstract QueryExecution query(Query query, TwdbTransaction transaction);
}
