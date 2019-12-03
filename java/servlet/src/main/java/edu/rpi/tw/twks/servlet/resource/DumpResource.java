package edu.rpi.tw.twks.servlet.resource;

import edu.rpi.tw.twks.api.Twks;
import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("dump")
public class DumpResource extends AbstractResource {
    public DumpResource() {
    }

    public DumpResource(final Twks twks) {
        super(twks);
    }

    @POST
    @Operation(
            summary = "Dump the contents of the store to the configured dump directory path (local to the server)"
    )
    public Response postDump() {
        try {
            getTwks().dump();
        } catch (final IOException e) {
            logger.error("exception on dump: ", e);
            return Response.serverError().build();
        }
        return Response.ok().build();
    }
}
