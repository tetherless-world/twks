package edu.rpi.tw.twdb.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public final class ServletContextListener extends GuiceServletContextListener {
    private Injector injector;

    @Override
    protected Injector getInjector() {
        if (injector == null) {
            injector = Guice.createInjector(new TwdbServletModule());
        }
        return injector;
    }

    private final static class TwdbServletModule extends ServletModule {
        @Override
        protected void configureServlets() {
            super.configureServlets();
        }
    }
}
