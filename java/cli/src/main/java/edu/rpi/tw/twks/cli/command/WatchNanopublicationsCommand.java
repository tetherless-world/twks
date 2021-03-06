package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.*;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.cli.CliNanopublicationParser;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationParser;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.methvin.watcher.DirectoryChangeEvent;
import io.methvin.watcher.DirectoryChangeListener;
import io.methvin.watcher.DirectoryWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public final class WatchNanopublicationsCommand extends Command {
    private final static String[] ALIASES = {};
    private final static String NAME = "watch-nanopublications";
    private final static Logger logger = LoggerFactory.getLogger(WatchNanopublicationsCommand.class);
    private final Args args = new Args();
    private @Nullable
    NanopublicationsDirectoryWatcher watcher = null;

    public WatchNanopublicationsCommand() {
        args.retry = 5;
    }

    @Override
    public final String[] getAliases() {
        return ALIASES;
    }

    @Override
    public final Args getArgs() {
        return args;
    }

    @Override
    public final String getName() {
        return NAME;
    }

    @Override
    public final void run(final TwksClient client, final MetricRegistry metricRegistry) {
        final Path directoryPath = Paths.get(args.directoryPath);
        final NanopublicationParser nanopublicationParser = new CliNanopublicationParser(args, metricRegistry);

        try {
            watcher = new NanopublicationsDirectoryWatcher(client, directoryPath, nanopublicationParser);
        } catch (final IOException e) {
            logger.error("error setting up directory watcher: ", e);
            return;
        }
        watcher.watch();
    }

    public final void stop() {
        if (watcher == null) {
            return;
        }

        try {
            watcher.close();
        } catch (final IOException e) {
            logger.error("error closing directory watcher: ", e);
        }
        watcher = null;
    }

    public final static class Args extends CliNanopublicationParser.Args {
        @Parameter(required = true, description = "path to a nanopublications directory")
        String directoryPath;
    }

    private final class NanopublicationsDirectoryWatcher implements DirectoryChangeListener {
        private final TwksClient client;
        private final Multimap<Path, Uri> currentNanopublicationUrisByPath = ArrayListMultimap.create();
        private final Path directoryPath;
        private final DirectoryWatcher directoryWatcher;
        private final NanopublicationParser nanopublicationParser;

        NanopublicationsDirectoryWatcher(final TwksClient client, final Path directoryPath, final NanopublicationParser nanopublicationParser) throws IOException {
            this.client = checkNotNull(client);
            this.directoryPath = checkNotNull(directoryPath);
            this.directoryWatcher = DirectoryWatcher.builder().path(this.directoryPath).listener(this).build();
            this.nanopublicationParser = checkNotNull(nanopublicationParser);
        }

        public final void close() throws IOException {
            directoryWatcher.close();
        }

        @Override
        public final void onEvent(final DirectoryChangeEvent event) throws IOException {
            logger.info("directory change event: {}", event);
            synchronize();
        }

        private final synchronized void synchronize() {
            final Set<Path> oldNanopublicationPaths = currentNanopublicationUrisByPath.keySet();
            final ImmutableMultimap<Path, Nanopublication> nanopublicationsByPath = nanopublicationParser.parseDirectory(directoryPath);
            final ImmutableSet<Path> nanopublicationPaths = nanopublicationsByPath.keySet();

            // #130: we were ignoring new nanopublications found by scanning the file system on DELETE events.
            // Ignore the file system change event type and scan the file system every time.

            {
                final ImmutableSet<Path> newNanopublicationPaths = Sets.difference(nanopublicationPaths, oldNanopublicationPaths).immutableCopy();
                if (!newNanopublicationPaths.isEmpty()) {
                    logger.info("{} new nanopublication paths: {}", newNanopublicationPaths.size(), newNanopublicationPaths);
                    final ImmutableList.Builder<Nanopublication> newNanopublicationsBuilder = ImmutableList.builder();
                    for (final Path newNanopublicationPath : newNanopublicationPaths) {
                        newNanopublicationsBuilder.addAll(nanopublicationsByPath.get(newNanopublicationPath));
                    }
                    final ImmutableList<Nanopublication> newNanopublications = newNanopublicationsBuilder.build();
                    if (logger.isInfoEnabled()) {
                        final ImmutableList<Uri> newNanopublicationUris = newNanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
                        logger.info("posted {} nanopublications from {} files: {}", newNanopublications.size(), newNanopublicationPaths.size(), newNanopublicationUris);
                    }
                    client.postNanopublications(newNanopublications);
                } else {
                    logger.debug("no new nanopublication paths");
                }
            }

            {
                final ImmutableSet<Path> deletedNanopublicationPaths = Sets.difference(oldNanopublicationPaths, nanopublicationPaths).immutableCopy();
                if (!deletedNanopublicationPaths.isEmpty()) {
                    logger.info("{} deleted nanopublication paths: {}", deletedNanopublicationPaths.size(), deletedNanopublicationPaths);
                    final ImmutableList.Builder<Uri> deletedNanopublicationUrisBuilder = ImmutableList.builder();
                    for (final Path deletedNanopublicationPath : deletedNanopublicationPaths) {
                        deletedNanopublicationUrisBuilder.addAll(currentNanopublicationUrisByPath.get(deletedNanopublicationPath));
                    }
                    final ImmutableList<Uri> deletedNanopublicationUris = deletedNanopublicationUrisBuilder.build();
                    client.deleteNanopublications(deletedNanopublicationUris);
                    logger.info("deleted {} nanopublications from {} files: {}", deletedNanopublicationUris.size(), deletedNanopublicationPaths.size(), deletedNanopublicationUris);
                } else {
                    logger.debug("no deleted nanopublication paths");
                }
            }

            {
                currentNanopublicationUrisByPath.clear();
                for (final Map.Entry<Path, Nanopublication> entry : nanopublicationsByPath.entries()) {
                    currentNanopublicationUrisByPath.put(entry.getKey(), entry.getValue().getUri());
                }
            }
        }

        public final void watch() {
            logger.info("watching {} for nanopublication changes", directoryPath);
            try {
                logger.info("initial scan");
                synchronize();
                directoryWatcher.watch();
            } catch (final ClosedWatchServiceException e) {
                logger.debug("watcher closed");
            }
            logger.info("exited watch of {} for nanopublication changes", directoryPath);
        }
    }
}
