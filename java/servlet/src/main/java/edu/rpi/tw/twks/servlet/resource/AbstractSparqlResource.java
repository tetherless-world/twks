package edu.rpi.tw.twks.servlet.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.servlet.AcceptLists;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.resultset.ResultSetLang;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

abstract class AbstractSparqlResource extends AbstractResource {
    protected final static AcceptList offerResultsAcceptList = AcceptLists.toAcceptList(ResultSetLang.SPARQLResultSetCSV, ResultSetLang.SPARQLResultSetJSON, ResultSetLang.SPARQLResultSetTSV, ResultSetLang.SPARQLResultSetXML);

    protected AbstractSparqlResource() {
    }

    protected AbstractSparqlResource(final Twks twks) {
        super(twks);
    }

    private final static List<Uri> parseUriList(final @Nullable List<String> uriStrings) {
        if (uriStrings == null || uriStrings.size() == 0) {
            return Collections.emptyList();
        }
        final List<Uri> uris = new ArrayList<>(uriStrings.size());
        for (final String uriString : uriStrings) {
            uris.add(Uri.parse(uriString));
        }
        return uris;
    }

    protected final Response
    doGet(
            @Nullable final String accept,
            @Nullable final List<String> defaultGraphUriStrings,
            @Nullable final List<String> namedGraphUriStrings,
            @Nullable final String queryString
    ) {
        return service(accept, defaultGraphUriStrings, namedGraphUriStrings, queryString);
    }

    protected final Response
    doPost(
            @Nullable final String accept,
            @Nullable final String contentType,
            @Nullable final List<String> defaultGraphUriStrings,
            @Nullable final List<String> namedGraphUriStrings,
            @Nullable final String queryString,
            final String requestBody
    ) {
        if (contentType != null && contentType.equalsIgnoreCase("application/sparql-query")) {
            // POST directly, query is the body
            return service(accept, defaultGraphUriStrings, namedGraphUriStrings, requestBody);
        } else {
            return service(accept, defaultGraphUriStrings, namedGraphUriStrings, queryString);
        }
    }

    protected abstract QueryExecution query(Query query, TwksTransaction transaction);

    protected Response
    service(
            @Nullable final String accept,
            @Nullable final List<String> defaultGraphUriStrings,
            @Nullable final List<String> namedGraphUriStrings,
            @Nullable final String queryString
    ) {
        final Optional<AcceptList> proposeAcceptList = AcceptLists.getProposeAcceptList(accept);

        if (queryString == null) {
            logger.error("missing query");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        final List<Uri> defaultGraphUris = parseUriList(defaultGraphUriStrings);
        final List<Uri> namedGraphUris = parseUriList(namedGraphUriStrings);

        final Query query;
        try {
            query = QueryFactory.create(queryString);
        } catch (final QueryException e) {
            logger.error("error parsing query '{}': ", queryString, e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        for (final Uri defaultGraphUri : defaultGraphUris) {
            query.addGraphURI(defaultGraphUri.toString());
        }
        for (final Uri namedGraphUri : namedGraphUris) {
            query.addNamedGraphURI(namedGraphUri.toString());
        }

        final Response.ResponseBuilder responseBuilder = Response.ok();

        try (final TwksTransaction transaction = getTwks().beginTransaction(ReadWrite.READ)) {
            try (final QueryExecution queryExecution = this.query(query, transaction)) {
                switch (query.getQueryType()) {
                    case Query.QueryTypeAsk:
                    case Query.QueryTypeSelect: {
                        final Lang respLang = AcceptLists.calculateResponseLang(ResultSetLang.SPARQLResultSetXML, offerResultsAcceptList, proposeAcceptList);

                        responseBuilder.header("Content-Type", respLang.getContentType().getContentType());

                        try (final ByteArrayOutputStream respOutputStream = new ByteArrayOutputStream()) {
                            if (query.getQueryType() == Query.QueryTypeAsk) {
                                final boolean result = queryExecution.execAsk();
                                ResultSetFormatter.output(respOutputStream, result, respLang);
                            } else {
                                final ResultSet resultSet = queryExecution.execSelect();
                                ResultSetFormatter.output(respOutputStream, resultSet, respLang);
                            }
                            responseBuilder.entity(respOutputStream.toByteArray());
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        }
                        break;
                    }
                    case Query.QueryTypeConstruct:
                    case Query.QueryTypeDescribe: {
                        final Lang respLang = AcceptLists.calculateResponseLang(Lang.TRIG, AcceptLists.OFFER_GRAPH, proposeAcceptList);

                        responseBuilder.header("Content-Type", respLang.getContentType().getContentType());

                        final Model respModel = query.getQueryType() == Query.QueryTypeConstruct ? queryExecution.execConstruct() : queryExecution.execDescribe();
                        try (final ByteArrayOutputStream respOutputStream = new ByteArrayOutputStream()) {
                            respModel.write(respOutputStream, respLang.getName());
                            responseBuilder.entity(respOutputStream.toByteArray());
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        }
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("" + query.getQueryType());
                }
            }
        }

        return responseBuilder.build();
    }
}
