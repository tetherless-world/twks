package edu.rpi.tw.twks.servlet.resource;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import edu.rpi.tw.twks.api.TwksVersion;
import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("version")
public class VersionResource extends AbstractResource {
    public VersionResource(final Twks twks) {
        super(twks);
    }

    @GET
    @Operation(
            summary = "Get the version of the server"
    )
    @Produces(MediaType.APPLICATION_JSON)
    public TwksVersion getVersion() {
        return TwksLibraryVersion.getInstance();
    }
}
