package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.servlet.resource.*;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyResourceConfig extends ResourceConfig {
    public JerseyResourceConfig(final Twks twks) {
        packages("io.swagger.v3.jaxrs2.integration.resources");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(new Factory<Twks>() {
                    @Override
                    public void dispose(final Twks twks) {
                    }

                    @Override
                    public Twks provide() {
                        return twks;
                    }
                }).to(Twks.class);
            }
        });
        register(AssertionsResource.class);
        register(AssertionsSparqlResource.class);
        register(DumpResource.class);
        register(NanopublicationResource.class);
        register(NanopublicationsSparqlResource.class);
        register(VersionResource.class);
    }
}
