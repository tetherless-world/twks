package edu.rpi.tw.twdb.server.servlet.nanopublication;

import com.google.inject.servlet.ServletModule;

public final class NanopublicationServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
//        serve("/sparql/assertions").with(AssertionsSparqlHttpServlet.class);
//        serve("/sparql/nanopublications").with(AssertionsSparqlHttpServlet.class);
    }
}
