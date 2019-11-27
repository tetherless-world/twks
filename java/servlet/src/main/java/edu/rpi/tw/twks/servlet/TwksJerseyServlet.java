package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.servlet.resource.AbstractResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public final class TwksJerseyServlet extends ServletContainer {
    public TwksJerseyServlet() {
        super(new ResourceConfig().packages(AbstractResource.class.getPackage().getName(), "io.swagger.v3.jaxrs2.integration.resources"));
    }
}
