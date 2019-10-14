package edu.rpi.tw.twks.server.servlet.sparql;

import com.google.common.io.CharStreams;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.server.AcceptLists;
import edu.rpi.tw.twks.server.ServletContextTwks;
import edu.rpi.tw.twks.server.servlet.TwksHttpServlet;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

abstract class SparqlHttpServlet extends TwksHttpServlet {
    private final static Logger logger = LoggerFactory.getLogger(SparqlHttpServlet.class);
    private final AcceptList offerResultsAcceptList;

    protected SparqlHttpServlet() {
        this(ServletContextTwks.getInstance());
    }

    protected SparqlHttpServlet(final Twks twks) {
        super(twks);
        offerResultsAcceptList = AcceptLists.toAcceptList(ResultSetLang.SPARQLResultSetCSV, ResultSetLang.SPARQLResultSetJSON, ResultSetLang.SPARQLResultSetTSV, ResultSetLang.SPARQLResultSetXML);
    }

    private static List<Uri> parseUriList(final @Nullable String[] uriStrings) {
        if (uriStrings == null || uriStrings.length == 0) {
            return Collections.emptyList();
        }
        final List<Uri> uris = new ArrayList<>(uriStrings.length);
        for (final String uriString : uriStrings) {
            uris.add(Uri.parse(uriString));
        }
        return uris;
    }

    @Override
    protected final void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (req.getContentType() == null || !req.getContentType().equalsIgnoreCase("application/sparql-query")) {
            doGetPostWithParameters(req, resp);
            return;
        }

        // POST directly, query is the body
        final String queryString = CharStreams.toString(req.getReader());

        service(Collections.emptyList(), Collections.emptyList(), queryString, req, resp);
    }

    @Override
    protected final void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
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
            query.addGraphURI(defaultGraphUri.toString());
        }
        for (final Uri namedGraphUri : namedGraphUris) {
            query.addNamedGraphURI(namedGraphUri.toString());
        }

        final Optional<AcceptList> proposeAcceptList = getProposeAcceptList(req);

        try (final TwksTransaction transaction = getDb().beginTransaction(ReadWrite.READ)) {
            try (final QueryExecution queryExecution = this.query(query, transaction)) {
                switch (query.getQueryType()) {
                    case Query.QueryTypeAsk:
                    case Query.QueryTypeSelect: {
                        final Lang respLang = AcceptLists.calculateResponseLang(ResultSetLang.SPARQLResultSetXML, offerResultsAcceptList, proposeAcceptList);

                        resp.setContentType(respLang.getContentType().getContentType());

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
                        final Lang respLang = AcceptLists.calculateResponseLang(Lang.TRIG, AcceptLists.OFFER_GRAPH, proposeAcceptList);

                        resp.setContentType(respLang.getContentType().getContentType());

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

    protected abstract QueryExecution query(Query query, TwksTransaction transaction);
}
