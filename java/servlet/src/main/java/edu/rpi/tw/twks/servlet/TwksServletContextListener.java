package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.api.TwksVersion;
import org.apache.commons.configuration2.web.ServletContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

public final class TwksServletContextListener implements javax.servlet.ServletContextListener {
    private final static Logger logger = LoggerFactory.getLogger(TwksServletContextListener.class);

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        TwksServletContext.destroyInstance();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();
        final TwksServletConfiguration configuration =
                TwksServletConfiguration.builder()
                        .setFromEnvironment()
                        .set(new ServletContextConfiguration(servletContextEvent.getServletContext())).build();
        TwksServletContext.initializeInstance(configuration);
        logger.info("twks-server " + TwksVersion.getInstance());
    }
}
