package edu.rpi.tw.twks.ext;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.observer.AsynchronousTwksObserver;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationTwksObserver;
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

public final class FileSystemExtensions {
    private final static Logger logger = LoggerFactory.getLogger(FileSystemExtensions.class);
    private final Path rootDirectoryPath;
    private final Optional<String> serverBaseUrl;

    public FileSystemExtensions(final Path rootDirectoryPath, final Optional<String> serverBaseUrl) {
        this.rootDirectoryPath = checkNotNull(rootDirectoryPath);
        this.serverBaseUrl = checkNotNull(serverBaseUrl);
    }

    public final void registerObservers(final Twks twks) {
        try {
            Files.list(rootDirectoryPath).forEach(subDirectoryPath -> {
                if (!Files.isDirectory(subDirectoryPath)) {
                    logger.warn("extfs: {} contains unrecognized non-directory {}", rootDirectoryPath, subDirectoryPath);
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
                            logger.info("extfs: {} is not owner-executable, ignoring", filePath);
                            return;
                        }

                        registerObserver(filePath, observerType, twks);
                    });
                } catch (final IOException e) {
                    logger.error("extfs: error listing subdirectory {}", subDirectoryPath);
                    return;
                }
            });
        } catch (final IOException e) {
            logger.error("extfs: error listing root directory {}", rootDirectoryPath);
        }
    }

    private void registerObserver(final Path filePath, final TwksObserverType type, final Twks twks) {
        switch (type) {
            case DELETE_NANOPUBLICATION:
                twks.registerDeleteNanopublicationObserver(new FileSystemDeleteNanopublicationTwksObserver(filePath));
                break;
            case PUT_NANOPUBLICATION:
                twks.registerPutNanopublicationObserver(new FileSystemPutNanopublicationTwksObserver(filePath));
                break;
            default:
                throw new UnsupportedOperationException();
        }
        logger.info("extfs: registered {} observer {}", type, filePath);
    }

    private enum TwksObserverType {
        DELETE_NANOPUBLICATION,
        PUT_NANOPUBLICATION
    }

    private final class FileSystemDeleteNanopublicationTwksObserver extends FileSystemTwksObserver implements DeleteNanopublicationTwksObserver {
        public FileSystemDeleteNanopublicationTwksObserver(final Path filePath) {
            super(filePath);
        }

        @Override
        public void onDeleteNanopublication(final Twks twks, final Uri nanopublicationUri) {
            final ProcessBuilder processBuilder = newProcessBuilder();
            processBuilder.command().add("--nanopublication-uri");
            processBuilder.command().add(nanopublicationUri.toString());
            runProcess(processBuilder);
        }
    }

    private final class FileSystemPutNanopublicationTwksObserver extends FileSystemTwksObserver implements PutNanopublicationTwksObserver {
        public FileSystemPutNanopublicationTwksObserver(final Path filePath) {
            super(filePath);
        }

        @Override
        public void onPutNanopublication(final Twks twks, final Nanopublication nanopublication) {
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
