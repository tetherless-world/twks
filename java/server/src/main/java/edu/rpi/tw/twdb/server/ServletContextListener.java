package edu.rpi.tw.twdb.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import edu.rpi.tw.twdb.server.servlet.sparql.SparqlServletModule;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public final class ServletContextListener extends GuiceServletContextListener {
    private Injector injector;
    private ServletContext servletContext;

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        this.servletContext = servletContextEvent.getServletContext();
        super.contextInitialized(servletContextEvent);
    }

    @Override
    protected Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(new SparqlServletModule(), new TwdbModule(servletContext));
        }
        return injector;
    }
}
