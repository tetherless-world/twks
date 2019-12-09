package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.api.Twks;
import org.glassfish.jersey.servlet.ServletContainer;

public final class JerseyServlet extends ServletContainer {
    public JerseyServlet(final Twks twks) {
        super(new JerseyResourceConfig(twks));
    }
}
