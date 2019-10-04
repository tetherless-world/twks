package edu.rpi.tw.twdb.server.servlet.sparql;

import com.google.inject.servlet.ServletModule;

public final class SparqlServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/sparql/assertions").with(AssertionsSparqlHttpServlet.class);
        serve("/sparql/nanopublications").with(AssertionsSparqlHttpServlet.class);
    }
}
