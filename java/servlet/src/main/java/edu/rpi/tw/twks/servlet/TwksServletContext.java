package edu.rpi.tw.twks.servlet;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.ext.ClasspathExtensions;
import edu.rpi.tw.twks.ext.FileSystemExtensions;
import edu.rpi.tw.twks.factory.TwksFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public final class TwksServletContext {
    private final static Logger logger = LoggerFactory.getLogger(TwksServletContext.class);
    private static TwksServletContext instance = null;
    private final ClasspathExtensions classpathExtensions;
    private final FileSystemExtensions fileSystemExtensions;
    private final Twks twks;

    private TwksServletContext(final TwksServletConfiguration configuration) {
        logger.info("creating servlet Twks instance with configuration {}", configuration);
        twks = TwksFactory.getInstance().createTwks(configuration.getFactoryConfiguration());
        classpathExtensions = new ClasspathExtensions(configuration.getExtcpDirectoryPath(), twks);
        fileSystemExtensions = new FileSystemExtensions(configuration.getExtfsDirectoryPath(), Optional.of(configuration.getServerBaseUrl()), twks);

        classpathExtensions.initialize();
        fileSystemExtensions.initialize();
    }

    synchronized static void destroyInstance() {
        if (instance != null) {
            instance.destroy();
        }
    }

    public final synchronized static TwksServletContext getInstance() {
        return checkNotNull(instance);
    }

    synchronized static void initializeInstance(final TwksServletConfiguration configuration) {
        checkState(instance == null);
        instance = new TwksServletContext(configuration);
    }

    private void destroy() {
        classpathExtensions.destroy();
        fileSystemExtensions.destroy();
    }

    public final Twks getTwks() {
        return twks;
    }
}
