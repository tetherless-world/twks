package edu.rpi.tw.twdb.server;

import com.google.inject.AbstractModule;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.lib.TwdbConfiguration;
import edu.rpi.tw.twdb.lib.TwdbFactory;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

final class TwdbModule extends AbstractModule {
    private final ServletContext servletContext;

    TwdbModule(final ServletContext servletContext) {
        this.servletContext = checkNotNull(servletContext);
    }

    @Override
    protected void configure() {
        final Properties attributeProperties = toProperties(servletContext.getAttributeNames(), name -> servletContext.getAttribute(name));
        final Properties initParameterProperties = toProperties(servletContext.getInitParameterNames(), name -> servletContext.getInitParameter(name));

        bind(Twdb.class).toInstance(TwdbFactory.getInstance().createTwdb(new TwdbConfiguration().setFromSystemProperties().setFromProperties(initParameterProperties).setFromProperties(attributeProperties)));
    }

    private Properties toProperties(final Enumeration<String> names, final Function<String, Object> valueGetter) {
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
