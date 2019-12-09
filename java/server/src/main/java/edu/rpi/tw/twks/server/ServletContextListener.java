package edu.rpi.tw.twks.server;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import edu.rpi.tw.twks.ext.ClasspathExtensions;
import edu.rpi.tw.twks.ext.FileSystemExtensions;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.servlet.JerseyServlet;
import org.apache.commons.configuration2.web.ServletContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;
import java.util.Optional;

public final class ServletContextListener implements javax.servlet.ServletContextListener {
    private final static Logger logger = LoggerFactory.getLogger(ServletContextListener.class);
    private ClasspathExtensions classpathExtensions;
    private FileSystemExtensions fileSystemExtensions;


    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        classpathExtensions.destroy();
        fileSystemExtensions.destroy();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();

        final TwksServerConfiguration configuration =
                TwksServerConfiguration.builder()
                        .setFromEnvironment()
                        .set(new ServletContextConfiguration(servletContext)).build();

        final Twks twks = TwksFactory.getInstance().createTwks(configuration.getFactoryConfiguration());

        classpathExtensions = new ClasspathExtensions(configuration.getExtcpDirectoryPath(), twks);
        fileSystemExtensions = new FileSystemExtensions(configuration.getExtfsDirectoryPath(), Optional.of(configuration.getServerBaseUrl()), twks);
        classpathExtensions.initialize();
        fileSystemExtensions.initialize();

        final ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(JerseyServlet.class.getSimpleName(), new JerseyServlet(twks));
        servletRegistration.addMapping("/*");

        logger.info("twks-server " + TwksLibraryVersion.getInstance());
    }
}
