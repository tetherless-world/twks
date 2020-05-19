package edu.rpi.tw.twks.server;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import edu.rpi.tw.twks.ext.ClasspathExtensions;
import edu.rpi.tw.twks.ext.FileSystemExtensions;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.nanopub.*;
import edu.rpi.tw.twks.servlet.JerseyServlet;
import org.apache.commons.configuration2.web.ServletContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRegistration;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

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

        final MetricRegistry metricRegistry = new MetricRegistry();

        logger.info("server configuration: {}", configuration);

        final Twks twks = TwksFactory.getInstance().createTwks(configuration.getFactoryConfiguration(), metricRegistry);

        classpathExtensions = new ClasspathExtensions(configuration.getExtcpDirectoryPath(), twks);
        fileSystemExtensions = new FileSystemExtensions(configuration.getExtfsDirectoryPath(), Optional.of(configuration.getServerBaseUrl()), twks);
        classpathExtensions.initialize();
        fileSystemExtensions.initialize();

        final ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(JerseyServlet.class.getSimpleName(), new JerseyServlet(twks));
        servletRegistration.addMapping("/*");

        logger.info("twks-server " + TwksLibraryVersion.getInstance());

        if (twks.isEmpty()) {
            loadInitialNanopublications(configuration, twks);
        } else {
            logger.info("store is not empty, skipping initial nanopublications if any");
        }
    }

    private void loadInitialNanopublications(final TwksServerConfiguration configuration, final Twks twks) {
        final NanopublicationParser nanopublicationParser = NanopublicationParser.builder().setDialect(NanopublicationDialect.SPECIFICATION).build();

        if (configuration.getInitialNanopublicationsDirectoryPath().isPresent()) {
            final InitialNanopublicationConsumer consumer = new InitialNanopublicationConsumer(twks) {
                @Override
                public void onMalformedNanopublicationException(final MalformedNanopublicationException exception) {
                    // Exception will be logged below.
                }
            };
            nanopublicationParser.parseDirectory(configuration.getInitialNanopublicationsDirectoryPath().get(), new NanopublicationDirectoryConsumer() {
                @Override
                public void accept(final Nanopublication nanopublication, final Path nanopublicationFilePath) {
                    consumer.accept(nanopublication);
                }

                @Override
                public void onMalformedNanopublicationException(final MalformedNanopublicationException exception, final Path nanopublicationFilePath) {
                    logger.error("error parsing nanopublication at {}", nanopublicationFilePath, exception);
                }
            });
            consumer.flush();
        }

        if (configuration.getInitialNanopublicationFilePaths().isPresent()) {
            for (final Path nanopublicationFilePath : configuration.getInitialNanopublicationFilePaths().get()) {
                final InitialNanopublicationConsumer consumer = new InitialNanopublicationConsumer(twks) {
                    @Override
                    public void onMalformedNanopublicationException(final MalformedNanopublicationException exception) {
                        logger.error("error parsing nanopublication at {}", nanopublicationFilePath, exception);
                    }
                };
                nanopublicationParser.parseFile(nanopublicationFilePath, consumer);
                consumer.flush();
//                logger.info("parsed {} initial nanopublication(s) from {}", fileNanopublications.size(), nanopublicationFilePath);
            }
        }
    }

    private abstract static class InitialNanopublicationConsumer implements NanopublicationConsumer {
        private final List<Nanopublication> nanopublicationsBuffer = new ArrayList<>();
        private final Twks twks;

        public InitialNanopublicationConsumer(final Twks twks) {
            this.twks = checkNotNull(twks);
        }

        @Override
        public final void accept(final Nanopublication nanopublication) {
            nanopublicationsBuffer.add(nanopublication);
            if (nanopublicationsBuffer.size() == 10) {
                logger.info("posting {} initial nanopublication(s)", nanopublicationsBuffer.size());
                twks.postNanopublications(ImmutableList.copyOf(nanopublicationsBuffer));
                logger.info("posted {} initial nanopublication(s)", nanopublicationsBuffer.size());
                nanopublicationsBuffer.clear();
            }
        }

        public final void flush() {
            if (nanopublicationsBuffer.isEmpty()) {
                return;
            }
            logger.info("posting {} initial nanopublication(s)", nanopublicationsBuffer.size());
            twks.postNanopublications(ImmutableList.copyOf(nanopublicationsBuffer));
            logger.info("posted {} initial nanopublication(s)", nanopublicationsBuffer.size());
            nanopublicationsBuffer.clear();
        }
    }
}
