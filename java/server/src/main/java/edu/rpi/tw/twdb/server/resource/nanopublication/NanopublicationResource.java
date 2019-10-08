package edu.rpi.tw.twdb.server.resource.nanopublication;

import edu.rpi.tw.nanopub.Uris;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.dmfs.rfc3986.Uri;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("nanopublication")
public class NanopublicationResource {
    @GET
    @Path("{nanopublicationUri}")
    public Response getNanopublication(@HeaderParam("Accept") @Nullable final String accept, @PathParam("nanopublicationUri") final String nanopublicationUriString) {
        final Uri nanopublicationUri = Uris.parse(nanopublicationUriString);

        return Response.ok().build();
    }
}
