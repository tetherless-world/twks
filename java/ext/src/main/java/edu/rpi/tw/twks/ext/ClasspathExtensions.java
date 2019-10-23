package edu.rpi.tw.twks.ext;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class ClasspathExtensions extends AbstractExtensions {
    private final static Logger logger = LoggerFactory.getLogger(ClasspathExtensions.class);
    private final ImmutableList<TwksExtension> extensions;

    public ClasspathExtensions(final Optional<Path> extDirectoryPath, final Twks twks) {
        super(twks);

        final Optional<ClassLoader> extClassLoader = getExtClassLoader(extDirectoryPath);
        final ServiceLoader<TwksExtension> serviceLoader;
        if (extClassLoader.isPresent()) {
            logger.info("loading classpath extensions from {}", extDirectoryPath.get());
            serviceLoader = ServiceLoader.load(TwksExtension.class, extClassLoader.get());
        } else {
            logger.info("loading classpath extensions from default classpaths");
            serviceLoader = ServiceLoader.load(TwksExtension.class);
        }

        final ImmutableList.Builder<TwksExtension> extensionsBuilder = ImmutableList.builder();
        for (final Iterator<TwksExtension> extensionI = serviceLoader.iterator(); extensionI.hasNext(); ) {
            final TwksExtension extension = extensionI.next();
            logger.info("loaded extension {}", extension.getClass().getCanonicalName());
            extensionsBuilder.add(extension);
        }
        extensions = extensionsBuilder.build();
        if (!extensions.isEmpty()) {
            logger.info("loaded {} classpath extensions", extensions.size());
        } else {
            logger.info("no classpath extensions loaded");
        }
    }

    private static Optional<ClassLoader> getExtClassLoader(final Optional<Path> extDirectoryPath) {
        if (!extDirectoryPath.isPresent()) {
            return Optional.empty();
        }

        if (!Files.exists(extDirectoryPath.get())) {
            logger.error("{} does not exist, not changing classpath", extDirectoryPath.get());
            return Optional.empty();
        }

        final List<Path> jarFilePaths = new ArrayList<>();
        if (Files.isDirectory(extDirectoryPath.get())) {
            try {
                Files.list(extDirectoryPath.get()).forEach(path -> {
                    if (Files.isRegularFile(path) && path.getFileName().toString().toLowerCase().endsWith(".jar")) {
                        jarFilePaths.add(path);
                    }
                });
            } catch (final IOException e) {
                logger.error("error listing {}", extDirectoryPath.get());
                return Optional.empty();
            }
        } else if (Files.isRegularFile(extDirectoryPath.get())) {
            jarFilePaths.add(extDirectoryPath.get());
        }

        final List<URL> jarFileUrls = new ArrayList<>();
        jarFilePaths.stream().forEach(jarFilePath -> {
            try {
                jarFileUrls.add(jarFilePath.toUri().toURL());
            } catch (final MalformedURLException e) {
                logger.error("{} could not be converted to a URL: ", jarFilePath, e);
            }
        });

        logger.info("extension .jar file URLs: {}", jarFileUrls);

        return Optional.of(new URLClassLoader(jarFileUrls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public final void destroy() {
        extensions.forEach(extensions -> extensions.destroy());
    }

    @Override
    public final void initialize() {
        extensions.forEach(extension -> extension.initialize(getTwks()));
    }
}
