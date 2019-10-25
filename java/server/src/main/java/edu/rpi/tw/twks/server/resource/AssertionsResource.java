package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.AcceptLists;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.StringWriter;

@Path("assertions")
public class AssertionsResource extends AbstractResource {
    public AssertionsResource() {
    }

    public AssertionsResource(final Twks twks) {
        super(twks);
    }

    @GET
    public Response getAssertions(
            @HeaderParam("Accept") @Nullable final String accept
    ) {
        final Lang responseLang = AcceptLists.calculateResponseLang(Lang.TRIG, AcceptLists.OFFER_DATASET, AcceptLists.getProposeAcceptList(accept));

        final Model assertions = getTwks().getAssertions();

        final Response.ResponseBuilder responseBuilder = Response.ok();
        responseBuilder.header("Content-Type", responseLang.getContentType().getContentType());
        final StringWriter responseStringWriter = new StringWriter();
        RDFDataMgr.write(responseStringWriter, assertions, responseLang);
        responseBuilder.entity(responseStringWriter.toString());

        return responseBuilder.build();
    }
}
