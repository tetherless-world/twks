package edu.rpi.tw.twks.server.resource.assertions;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.server.resource.AbstractResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("assertions")
public class AssertionsResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(AssertionsResource.class);

    public AssertionsResource() {
    }

    public AssertionsResource(final Twks twks) {
        super(twks);
    }

    @GET
    public Response getAssertions() {
    }

}
