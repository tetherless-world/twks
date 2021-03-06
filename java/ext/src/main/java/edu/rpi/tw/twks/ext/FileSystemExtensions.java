package edu.rpi.tw.twks.ext;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.observer.AsynchronousTwksObserver;
import edu.rpi.tw.twks.api.observer.ChangeObserver;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationObserver;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.uri.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class FileSystemExtensions extends AbstractExtensions {
    private final static Logger logger = LoggerFactory.getLogger(FileSystemExtensions.class);
    private final Path rootDirectoryPath;
    private final Optional<String> serverBaseUrl;

    public FileSystemExtensions(final Path rootDirectoryPath, final Optional<String> serverBaseUrl, final Twks twks) {
        super(twks);
        this.rootDirectoryPath = checkNotNull(rootDirectoryPath);
        this.serverBaseUrl = checkNotNull(serverBaseUrl);
    }

    @Override
    public final void destroy() {
    }

    @Override
    public final void initialize() {
        if (!Files.isDirectory(rootDirectoryPath)) {
            logger.warn("{} does not exist, disabling file system extensions", rootDirectoryPath);
            return;
        }

        logger.info("found {}, enabling file system extensions", rootDirectoryPath);

        final Path observerDirectoryPath = rootDirectoryPath.resolve("observer");
        try {
            Files.list(observerDirectoryPath).forEach(subDirectoryPath -> {
                if (!Files.isDirectory(subDirectoryPath)) {
                    logger.debug("extfs: {} contains unrecognized non-directory {}", observerDirectoryPath, subDirectoryPath);
                    return;
                }

                final TwksObserverType observerType;
                try {
                    observerType = TwksObserverType.valueOf(subDirectoryPath.getFileName().toString().toUpperCase());
                } catch (final IllegalArgumentException e) {
                    logger.error("extfs: {}: unrecognized observer type {}", subDirectoryPath, subDirectoryPath.getFileName());
                    return;
                }

                try {
                    Files.list(subDirectoryPath).forEach(filePath -> {
                        if (!Files.isRegularFile(filePath)) {
                            logger.warn("extfs: {} contains unrecognized non-file {}", subDirectoryPath, filePath);
                            return;
                        }

                        final Set<PosixFilePermission> filePermissions;
                        try {
                            filePermissions = Files.getPosixFilePermissions(filePath);
                        } catch (final IOException e) {
                            logger.error("extfs: error getting file permissions for {}", filePath);
                            return;
                        }

                        if (!filePermissions.contains(PosixFilePermission.OWNER_EXECUTE)) {
                            logger.debug("extfs: {} is not owner-executable, ignoring", filePath);
                            return;
                        }

                        registerObserver(filePath, observerType, getTwks());
                    });
                } catch (final IOException e) {
                    logger.error("extfs: error listing subdirectory {}", subDirectoryPath);
                    return;
                }
            });
        } catch (final IOException e) {
            logger.error("extfs: error listing observer directory {}", observerDirectoryPath);
        }
    }

    private void registerObserver(final Path filePath, final TwksObserverType type, final Twks twks) {
        switch (type) {
            case CHANGE:
                twks.registerChangeObserver(new FileSystemChangeObserver(filePath));
                break;
            case DELETE_NANOPUBLICATION:
                twks.registerDeleteNanopublicationObserver(new FileSystemDeleteNanopublicationObserver(filePath));
                break;
            case PUT_NANOPUBLICATION:
                twks.registerPutNanopublicationObserver(new FileSystemPutNanopublicationObserver(filePath));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        logger.info("extfs: registered {} observer {}", type, filePath);
    }

    private enum TwksObserverType {
        CHANGE,
        DELETE_NANOPUBLICATION,
        PUT_NANOPUBLICATION
    }

    private final class FileSystemChangeObserver extends FileSystemTwksObserver implements ChangeObserver {
        public FileSystemChangeObserver(final Path filePath) {
            super(filePath);
        }

        @Override
        public void onChange() {
            runProcess(newProcessBuilder());
        }
    }

    private final class FileSystemDeleteNanopublicationObserver extends FileSystemTwksObserver implements DeleteNanopublicationObserver {
        public FileSystemDeleteNanopublicationObserver(final Path filePath) {
            super(filePath);
        }

        @Override
        public void onDeleteNanopublication(final Uri nanopublicationUri) {
            final ProcessBuilder processBuilder = newProcessBuilder();
            processBuilder.command().add("--nanopublication-uri");
            processBuilder.command().add(nanopublicationUri.toString());
            runProcess(processBuilder);
        }
    }

    private final class FileSystemPutNanopublicationObserver extends FileSystemTwksObserver implements PutNanopublicationObserver {
        public FileSystemPutNanopublicationObserver(final Path filePath) {
            super(filePath);
        }

        @Override
        public void onPutNanopublication(final Nanopublication nanopublication) {
            final ProcessBuilder processBuilder = newProcessBuilder();
            processBuilder.command().add("--nanopublication-uri");
            processBuilder.command().add(nanopublication.getUri().toString());
            runProcess(processBuilder);
        }
    }

    private abstract class FileSystemTwksObserver implements AsynchronousTwksObserver {
        private final Path filePath;

        protected FileSystemTwksObserver(final Path filePath) {
            this.filePath = checkNotNull(filePath);
        }

        protected final ProcessBuilder newProcessBuilder() {
            final ProcessBuilder processBuilder = new ProcessBuilder(filePath.toString());
            processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            if (serverBaseUrl.isPresent()) {
                processBuilder.command().add("--server-base-url");
                processBuilder.command().add(serverBaseUrl.get());
            }
            return processBuilder;
        }

        protected final void runProcess(final ProcessBuilder processBuilder) {
            try {
                final Process process = processBuilder.start();
                final int returnCode = process.waitFor();
                if (returnCode != 0) {
                    logger.warn("extfs: {} returned {}", filePath, returnCode);
                }
            } catch (final IOException | InterruptedException e) {
                logger.error("extfs: error executing {}: ", filePath, e);
            }
        }
    }
}
