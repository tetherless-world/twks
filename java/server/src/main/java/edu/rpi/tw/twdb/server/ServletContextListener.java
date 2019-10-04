package edu.rpi.tw.twdb.server;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.lib.Tdb2Twdb;
import edu.rpi.tw.twdb.server.servlet.sparql.SparqlServletModule;

public final class ServletContextListener extends GuiceServletContextListener {
    private Injector injector;

    @Override
    protected Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(new SparqlServletModule(), new TwdbModule());
        }
        return injector;
    }

    private final static class TwdbModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Twdb.class).toInstance(new Tdb2Twdb());
        }
    }
}
