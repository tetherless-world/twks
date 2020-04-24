package edu.rpi.tw.twks.cli.command;

import com.beust.jcommander.Parameter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.cli.CliNanopublicationParser;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationException;
import edu.rpi.tw.twks.nanopub.MalformedNanopublicationRuntimeException;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationConsumer;
import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class PostNanopublicationsCommand extends Command {
    private final static String[] ALIASES = {"post", "put", "put-nanopublications"};
    private final static String NAME = "post-nanopublications";
    private final static Logger logger = LoggerFactory.getLogger(PostNanopublicationsCommand.class);
    private final Args args = new Args();

    @Override
    public String[] getAliases() {
        return ALIASES;
    }

    @Override
    public Args getArgs() {
        return args;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void run(final TwksClient client, final MetricRegistry metricRegistry) {
        final CliNanopublicationParser parser = new CliNanopublicationParser(args, metricRegistry);

        try (final ProgressBar progressBar = new ProgressBarBuilder()
                .setInitialMax(0)
                .setTaskName("Posted nanopublications")
                .setConsumer(new DelegatingProgressBarConsumer(logger::info))
                .showSpeed()
                .build()) {
            final BufferingNanopublicationConsumer consumer = new BufferingNanopublicationConsumer(client, metricRegistry, progressBar);

            if (args.sources.isEmpty()) {
                parser.parseStdin(consumer);
            } else {
                for (final String source : args.sources) {
                    parser.parse(source, consumer);
                }
            }

            consumer.flush();
        }
    }

    public final static class Args extends CliNanopublicationParser.Args {
        @Parameter(names = {"--continue-on-malformed-nanopublication"})
        boolean continueOnMalformedNanopublication = false;
        @Parameter(names = {"--nanopublications-buffer-size"}, description = "nanopublications buffer size")
        int nanopublicationsBufferSize = 10;
        @Parameter(required = true, description = "1+ nanopublication or assertion file path(s) or URI(s), or - for stdin")
        List<String> sources = new ArrayList<>();
    }

    private final class BufferingNanopublicationConsumer implements NanopublicationConsumer {
        private final TwksClient client;
        private final List<Nanopublication> nanopublicationsBuffer = new ArrayList<>();
        private final Timer postNanopublicationsTimer;
        private final ProgressBar progressBar;
        private int postedNanopublicationsCount = 0;

        public BufferingNanopublicationConsumer(final TwksClient client, final MetricRegistry metricRegistry, final ProgressBar progressBar) {
            postNanopublicationsTimer = metricRegistry.timer(MetricRegistry.name(PostNanopublicationsCommand.class, "postNanopublicationsTimer"));
            this.client = checkNotNull(client);
            this.progressBar = checkNotNull(progressBar);
        }

        @Override
        public final void accept(final Nanopublication nanopublication) {
            final ImmutableList<Nanopublication> nanopublicationsToPost;
            synchronized (nanopublicationsBuffer) {
                nanopublicationsBuffer.add(nanopublication);
                if (nanopublicationsBuffer.size() < args.nanopublicationsBufferSize) {
                    return;
                }
                // Make a copy of the buffer and then exit the synchronized block so it can continue buffering.
                nanopublicationsToPost = ImmutableList.copyOf(nanopublicationsBuffer);
                nanopublicationsBuffer.clear();
            }
            postNanopublications(nanopublicationsToPost);
        }

        public final void flush() {
            final ImmutableList<Nanopublication> nanopublicationsToPost;
            synchronized (nanopublicationsBuffer) {
                if (nanopublicationsBuffer.isEmpty()) {
                    return;
                }
                // Make a copy of the buffer and then exit the synchronized block so it can continue buffering.
                nanopublicationsToPost = ImmutableList.copyOf(nanopublicationsBuffer);
                nanopublicationsBuffer.clear();
            }
            postNanopublications(nanopublicationsToPost);
        }

        @Override
        public final void onMalformedNanopublicationException(final MalformedNanopublicationException exception) {
            if (args.continueOnMalformedNanopublication) {
                logger.error("malformed nanopublication exception: ", exception);
            } else {
                throw new MalformedNanopublicationRuntimeException(exception);
            }
        }

        private void postNanopublications(final ImmutableList<Nanopublication> nanopublicationsToPost) {
            try (final Timer.Context timerContext = postNanopublicationsTimer.time()) {
                client.postNanopublications(nanopublicationsToPost);
            }
            postedNanopublicationsCount += nanopublicationsToPost.size();
//            logger.info("posted {} new nanopublication(s) ({} total)", nanopublicationsToPost.size(), postedNanopublicationsCount);
            progressBar.stepBy(nanopublicationsToPost.size());
        }
    }
}
