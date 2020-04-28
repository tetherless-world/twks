package edu.rpi.tw.twks.servlet.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/sparql/nanopublications")
public final class NanopublicationsSparqlResource extends AbstractSparqlResource {
    @Inject
    public NanopublicationsSparqlResource(final Twks twks) {
        super(twks);
    }

    @GET
    @Operation(
            externalDocs = @ExternalDocumentation(url = "https://www.w3.org/TR/sparql11-protocol/"),
            summary = "Query triples in all named graphs using SPARQL"
    )
    public final Response
    get(
            @HeaderParam("Accept") @Nullable final AcceptList accept,
            @QueryParam("default-graph-uri") @Nullable final List<String> defaultGraphUriStrings,
            @QueryParam("named-graph-uri") @Nullable final List<String> namedGraphUriStrings,
            @QueryParam("query") @Nullable final String queryString
    ) {
        return doGet(accept, defaultGraphUriStrings, namedGraphUriStrings, queryString);
    }

    @POST
    @Operation(
            externalDocs = @ExternalDocumentation(url = "https://www.w3.org/TR/sparql11-protocol/"),
            summary = "Query triples in all named graphs using SPARQL"
    )
    public final Response
    post(
            @HeaderParam("Accept") @Nullable final AcceptList accept,
            @HeaderParam("Content-Type") @Nullable final MediaType contentType,
            final String requestBody
    ) {
        return doPost(accept, contentType, requestBody);
    }

    @Override
    protected final QueryExecution query(final Query query, final TwksTransaction transaction) {
        return transaction.queryNanopublications(query);
    }
}
