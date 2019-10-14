package edu.rpi.tw.twks.server;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.core.TwksConfiguration;
import edu.rpi.tw.twks.core.TwksFactory;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServletContextTwks {
    private static Twks instance = null;

    private ServletContextTwks() {
    }

    synchronized static void initInstance(final ServletContext servletContext) {
        final Properties attributeProperties = toProperties(servletContext.getAttributeNames(), name -> servletContext.getAttribute(name));
        final Properties initParameterProperties = toProperties(servletContext.getInitParameterNames(), name -> servletContext.getInitParameter(name));

        instance = TwksFactory.getInstance().createTwks(new TwksConfiguration().setFromSystemProperties().setFromProperties(initParameterProperties).setFromProperties(attributeProperties));
    }

    public final synchronized static Twks getInstance() {
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
