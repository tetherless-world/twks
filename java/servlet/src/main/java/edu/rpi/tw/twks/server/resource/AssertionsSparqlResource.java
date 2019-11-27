package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/sparql/assertions")
public final class AssertionsSparqlResource extends AbstractSparqlResource {
    public AssertionsSparqlResource(final Twks twks) {
        super(twks);
    }

    public AssertionsSparqlResource() {
    }

    @GET
    public final Response
    get(
            @HeaderParam("Accept") @Nullable final String accept,
            @QueryParam("default-graph-uri") @Nullable final List<String> defaultGraphUriStrings,
            @QueryParam("named-graph-uri") @Nullable final List<String> namedGraphUriStrings,
            @QueryParam("query") @Nullable final String queryString
    ) {
        return doGet(accept, defaultGraphUriStrings, namedGraphUriStrings, queryString);
    }

    @POST
    public final Response
    post(
            @HeaderParam("Accept") @Nullable final String accept,
            @HeaderParam("Content-Type") @Nullable final String contentType,
            @QueryParam("default-graph-uri") @Nullable final List<String> defaultGraphUriStrings,
            @QueryParam("named-graph-uri") @Nullable final List<String> namedGraphUriStrings,
            @QueryParam("query") @Nullable final String queryString,
            final String requestBody
    ) {
        return doPost(accept, contentType, defaultGraphUriStrings, namedGraphUriStrings, queryString, requestBody);
    }

    @Override
    protected final QueryExecution query(final Query query, final TwksTransaction transaction) {
        return transaction.queryAssertions(query);
    }
}
