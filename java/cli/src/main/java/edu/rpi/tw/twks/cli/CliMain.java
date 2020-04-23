package edu.rpi.tw.twks.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksClient;
import edu.rpi.tw.twks.api.TwksLibraryVersion;
import edu.rpi.tw.twks.cli.command.*;
import edu.rpi.tw.twks.client.direct.DirectTwksClient;
import edu.rpi.tw.twks.client.rest.RestTwksClient;
import edu.rpi.tw.twks.client.rest.RestTwksClientConfiguration;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class CliMain {
    private final static Command[] commands = {
            new DeleteNanopublicationsCommand(),
            new DumpCommand(),
            new PostNanopublicationsCommand(),
            new QueryCommand(),
            new WatchNanopublicationsCommand()
    };
    private final static Logger logger = LoggerFactory.getLogger(CliMain.class);

    public static void main(final String[] argv) {
        final JCommander.Builder jCommanderBuilder = JCommander.newBuilder();
        jCommanderBuilder.programName("twks-cli " + TwksLibraryVersion.getInstance());

        final GlobalArgs globalArgs = new GlobalArgs();
        jCommanderBuilder.addObject(globalArgs);

        // Commands
        final Map<String, Command> commandsByName = new HashMap<>();
        for (final Command command : commands) {
            jCommanderBuilder.addCommand(command.getName(), command.getArgs(), command.getAliases());
            commandsByName.put(command.getName(), command);
        }

        final JCommander jCommander = jCommanderBuilder.build();

        jCommander.parse(argv);

        if (globalArgs.help) {
            jCommander.usage();
            return;
        }

        if (globalArgs.version) {
            System.out.println(TwksLibraryVersion.getInstance());
            return;
        }

        if (jCommander.getParsedCommand() == null) {
            jCommander.usage();
            return;
        }

        final Command command = commandsByName.get(jCommander.getParsedCommand());

        final MetricRegistry metricRegistry = new MetricRegistry();

        final PropertiesConfiguration configurationProperties = new PropertiesConfiguration();

        if (globalArgs.configurationFilePath != null) {
            try (final FileReader fileReader = new FileReader(new File(globalArgs.configurationFilePath))) {
                configurationProperties.read(fileReader);
            } catch (final ConfigurationException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        globalArgs.configuration.forEach((key, value) -> configurationProperties.setProperty(key, value));

        final TwksClient client;

        final TwksFactoryConfiguration.Builder configurationBuilder = TwksFactoryConfiguration.builder().setFromEnvironment().set(configurationProperties);
        if (configurationBuilder.isDirty()) {
            // Write to a TWKS instance directly in the CLI process.
            final TwksFactoryConfiguration configuration = configurationBuilder.build();
            final Twks twks = TwksFactory.getInstance().createTwks(configuration);
            logger.info("using library implementation {} with configuration {}", twks.getClass().getCanonicalName(), configuration);
            client = new DirectTwksClient(twks);
        } else {
            // Write to a TWKS server.
            final RestTwksClientConfiguration.Builder clientConfigurationBuilder = RestTwksClientConfiguration.builder();
            clientConfigurationBuilder.setFromEnvironment();
            // Support both -Dkey=value and -Dtwks.key=value
            clientConfigurationBuilder.set(configurationProperties);
            clientConfigurationBuilder.set(configurationProperties.subset("twks"));
            final RestTwksClientConfiguration clientConfiguration = clientConfigurationBuilder.build();
            logger.debug("using client with configuration {}", clientConfiguration);
            client = new RestTwksClient(clientConfiguration);
        }

        @Nullable ConsoleReporter reporter = null;
        if (globalArgs.reportMetrics) {
            reporter = ConsoleReporter.forRegistry(metricRegistry).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
            reporter.start(1, TimeUnit.SECONDS);
        }

        command.run(client, metricRegistry);

        if (reporter != null) {
            reporter.report();
        }
    }

    private final static class GlobalArgs {
        @DynamicParameter(names = "-D", description = "library configuration, overrides -c and system properties")
        Map<String, String> configuration = new HashMap<>();

        @Parameter(names = {"-c"}, description = "library configuration file path in .properties format")
        String configurationFilePath;

        @Parameter(names = {"-h", "--help"})
        boolean help = false;

        @Parameter(names = {"--report-metrics"})
        boolean reportMetrics = false;

        @Parameter(names = {"-v", "--version"})
        boolean version = false;
    }
}
