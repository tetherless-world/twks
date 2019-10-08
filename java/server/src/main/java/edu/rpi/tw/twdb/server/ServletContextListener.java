package edu.rpi.tw.twdb.server;

import javax.servlet.ServletContextEvent;

public final class ServletContextListener implements javax.servlet.ServletContextListener {
    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        ServletContextTwdb.initInstance(servletContextEvent.getServletContext());
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
    }
}
