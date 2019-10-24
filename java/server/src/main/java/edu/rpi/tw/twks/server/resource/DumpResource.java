package edu.rpi.tw.twks.server.resource;

import edu.rpi.tw.twks.api.Twks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("dump")
public class DumpResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(DumpResource.class);

    public DumpResource() {
    }

    public DumpResource(final Twks twks) {
        super(twks);
    }

    @GET
    public Response getDump() {
        try {
            getTwks().dump();
        } catch (final IOException e) {
            logger.error("exception on dump: ", e);
            return Response.serverError().build();
        }
        return Response.ok().build();
    }
}
