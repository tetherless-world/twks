package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.api.TwksVersion;
import org.apache.commons.configuration2.web.ServletContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Function;

public final class TwksServletContextListener implements javax.servlet.ServletContextListener {
    private final static Logger logger = LoggerFactory.getLogger(TwksServletContextListener.class);

    private static Properties toProperties(final Enumeration<String> names, final Function<String, Object> valueGetter) {
        final Properties properties = new Properties();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final Object value = valueGetter.apply(name);
            if (value instanceof String) {
                properties.setProperty(name, (String) value);
//                System.out.println(name + " = " + value);
            }
        }
        return properties;
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        TwksServletContext.destroyInstance();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();
        final TwksServletConfiguration configuration = TwksServletConfiguration.builder().setFromEnvironment().set(new ServletContextConfiguration(servletContextEvent.getServletContext())).build();
        TwksServletContext.initializeInstance(configuration);
        logger.info("twks-server " + TwksVersion.getInstance());
    }
}
