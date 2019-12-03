package edu.rpi.tw.twks.servlet.resource;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.servlet.AcceptLists;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.List;

@Path("assertions")
public class AssertionsResource extends AbstractResource {
    public AssertionsResource() {
    }

    public AssertionsResource(final Twks twks) {
        super(twks);
    }

    @GET
    @Operation(
            responses = {
                    @ApiResponse(
                            responseCode = "200"
                    )
            },
            summary = "Get all assertions in the store as RDF triples"
    )
    public Response getAssertions(
            @HeaderParam("Accept") @Nullable @Parameter(description = "Accept header, defaults to text/trig") final String accept
    ) {
        final Model assertions = getTwks().getAssertions();
        return getAssertionsDelegate(accept, assertions);
    }

    private Response getAssertionsDelegate(@Nullable final String accept, final Model assertions) {
        final Lang responseLang = AcceptLists.calculateResponseLang(Lang.TRIG, AcceptLists.OFFER_DATASET, AcceptLists.getProposeAcceptList(accept));

        final Response.ResponseBuilder responseBuilder = Response.ok();
        responseBuilder.header("Content-Type", responseLang.getContentType().getContentType());
        final StringWriter responseStringWriter = new StringWriter();
        RDFDataMgr.write(responseStringWriter, assertions, responseLang);
        responseBuilder.entity(responseStringWriter.toString());

        return responseBuilder.build();
    }

    @GET
    @Operation(
            responses = {
                    @ApiResponse(
                            responseCode = "200"
                    )
            },
            summary = "Get assertions that are associated with a specific set of ontologies"
    )
    @Path("ontology")
    public Response getOntologyAssertions(
            @HeaderParam("Accept") @Nullable @Parameter(description = "Accept header, defaults to text/trig") final String accept,
            @QueryParam("uri") @Parameter(description = "one or more ontology URIs") final List<String> ontologyUriStrings
    ) {
        final ImmutableSet<Uri> ontologyUris = ontologyUriStrings.stream().map(uriString -> Uri.parse(uriString)).collect(ImmutableSet.toImmutableSet());
        final Model assertions = getTwks().getOntologyAssertions(ontologyUris);
        return getAssertionsDelegate(accept, assertions);
    }
}
