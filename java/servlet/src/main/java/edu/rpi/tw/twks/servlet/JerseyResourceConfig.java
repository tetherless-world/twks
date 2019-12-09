package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.servlet.resource.*;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyResourceConfig extends ResourceConfig {
    public JerseyResourceConfig(final Twks twks) {
        packages("io.swagger.v3.jaxrs2.integration.resources");
        register(new AssertionsResource(twks));
        register(new AssertionsSparqlResource(twks));
        register(new DumpResource(twks));
        register(new NanopublicationResource(twks));
        register(new NanopublicationsSparqlResource(twks));
        register(new VersionResource(twks));
    }
}
