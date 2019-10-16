package edu.rpi.tw.twks.server;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.ext.ClasspathExtensions;
import edu.rpi.tw.twks.ext.FileSystemExtensions;
import edu.rpi.tw.twks.factory.TwksFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ServletTwks {
    private final static Logger logger = LoggerFactory.getLogger(ServletTwks.class);
    private static Twks instance = null;

    private ServletTwks() {
    }

    synchronized static void initInstance(final ServletConfiguration configuration) {
        logger.info("creating servlet Twks instance with configuration {}", configuration);
        instance = TwksFactory.getInstance().createTwks(configuration);

        logger.info("enabling classpath extensions");
        new ClasspathExtensions().registerObservers(instance);

        final Path extfsDirectoryPath = Paths.get(configuration.getExtfsDirectoryPath());
        if (Files.isDirectory(extfsDirectoryPath)) {
            logger.info("found {}, enabling file system extensions", extfsDirectoryPath);
            new FileSystemExtensions(extfsDirectoryPath, Optional.of(configuration.getServerBaseUrl())).registerObservers(instance);
        } else {
            logger.warn("{} does not exist, disabling file system extensions", extfsDirectoryPath);
        }
    }

    public final synchronized static Twks getInstance() {
        return checkNotNull(instance);
    }
}
