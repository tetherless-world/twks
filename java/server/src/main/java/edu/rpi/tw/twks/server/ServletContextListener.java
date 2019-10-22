package edu.rpi.tw.twks.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Function;

public final class ServletContextListener implements javax.servlet.ServletContextListener {
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
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();
        final Properties attributeProperties = toProperties(servletContext.getAttributeNames(), name -> servletContext.getAttribute(name));
        final Properties initParameterProperties = toProperties(servletContext.getInitParameterNames(), name -> servletContext.getInitParameter(name));
        final ServletConfiguration configuration = (ServletConfiguration) new ServletConfiguration().setFromSystemProperties().setFromProperties(initParameterProperties).setFromProperties(attributeProperties);
        ServletTwks.initializeInstance(configuration);
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        ServletTwks.destroyInstance();
    }
}
