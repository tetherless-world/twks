package edu.rpi.tw.twks.cli;

import com.beust.jcommander.Parameter;
import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.nanopub.*;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public final class CliNanopublicationParser extends NanopublicationParser {
    private final static Logger logger = LoggerFactory.getLogger(CliNanopublicationParser.class);
    private final int retry;
    private final int retryDelayS;

    public CliNanopublicationParser(final Args args, final MetricRegistry metricRegistry) {
        super(
                args.dialect != null ? NanopublicationDialect.valueOf(args.dialect.toUpperCase()) : NanopublicationDialect.SPECIFICATION,
                args.lang != null ? Optional.ofNullable(RDFLanguages.shortnameToLang(args.lang)) : Optional.empty(),
                metricRegistry
        );
        this.retry = args.retry >= 0 ? args.retry : 0;
        this.retryDelayS = args.retrayDelayS > 0 ? args.retrayDelayS : 1;
    }

//    public CliNanopublicationParser(final NanopublicationDialect dialect, final Optional<Lang> lang) {
//        this.dialect = checkNotNull(dialect);
//        this.lang = checkNotNull(lang);
//    }

//    private ImmutableList<Uri> getNanopublicationUris(final ImmutableList<Nanopublication> nanopublications) {
//        return nanopublications.stream().map(nanopublication -> nanopublication.getUri()).collect(ImmutableList.toImmutableList());
//    }

    @Override
    public final void parseFile(final Path sourceFilePath, final NanopublicationConsumer consumer) {
        if (retry == 0) {
            super.parseFile(sourceFilePath, consumer);
        } else {
            parseFileWithRetry(sourceFilePath, consumer);
        }
    }

    private void parseFileWithRetry(final Path sourceFilePath, final NanopublicationConsumer consumer) {
        final int tryMax = retry + 1;
        for (int tryI = 0; tryI < tryMax; tryI++) {
            try {
                final BoxedBoolean parsedNanopublication = new BoxedBoolean();
                super.parseFile(sourceFilePath, new NanopublicationConsumer() {
                    @Override
                    public void accept(final Nanopublication nanopublication) {
                        consumer.accept(nanopublication);
                        parsedNanopublication.set(true);
                    }

                    @Override
                    public void onMalformedNanopublicationException(final MalformedNanopublicationException exception) {
                        consumer.onMalformedNanopublicationException(exception);
                    }
                });
                if (parsedNanopublication.get()) {
                    return;
                }
            } catch (final RiotNotFoundException e) {
                logger.error("nanopublication file {} not found", sourceFilePath);
                return;
            } catch (final MalformedNanopublicationRuntimeException e) {
            }

            logger.error("error parsing {}, try {}", sourceFilePath, tryI);
            if (tryI + 1 < tryMax) {
                logger.error("retrying {} parse after {} seconds", sourceFilePath, retryDelayS);
                try {
                    Thread.sleep(retryDelayS);
                } catch (final InterruptedException ex) {
                    return;
                }
            }
        }
    }

    public abstract static class Args {
        @Parameter(names = {"--dialect"}, description = "dialect of the nanopublication, such as SPECIFICATION or WHYIS")
        public String dialect = null;

        @Parameter(names = {"-l", "--lang"}, description = "language/format of the nanopublication file e.g., TRIG")
        public String lang = null;

        @Parameter(names = {"--retry-delay-s"}, description = "delay between retries")
        public int retrayDelayS = 1;
        @Parameter(names = {"--retry"}, description = "retry parsing a file n times after a short delay, defaults to no retries")
        public int retry = 0;
    }

    private final static class BoxedBoolean {
        private boolean value = false;

        public boolean get() {
            return value;
        }

        public void set(final boolean value) {
            this.value = value;
        }
    }
}
