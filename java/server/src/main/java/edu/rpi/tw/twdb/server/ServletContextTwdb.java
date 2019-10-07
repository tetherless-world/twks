package edu.rpi.tw.twdb.server;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.lib.TwdbConfiguration;
import edu.rpi.tw.twdb.lib.TwdbFactory;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class ServletContextTwdb {
    private static Twdb instance = null;

    private ServletContextTwdb() {
    }

    synchronized static void initInstance(final ServletContext servletContext) {
        checkState(instance == null);
        final Properties attributeProperties = toProperties(servletContext.getAttributeNames(), name -> servletContext.getAttribute(name));
        final Properties initParameterProperties = toProperties(servletContext.getInitParameterNames(), name -> servletContext.getInitParameter(name));

        instance = TwdbFactory.getInstance().createTwdb(new TwdbConfiguration().setFromSystemProperties().setFromProperties(initParameterProperties).setFromProperties(attributeProperties));
    }

    public final synchronized static Twdb getInstance() {
        return checkNotNull(instance);
    }

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
}
