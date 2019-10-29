package edu.rpi.tw.twks.cli;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.cli.command.Command;
import edu.rpi.tw.twks.cli.command.DeleteNanopublicationsCommand;
import edu.rpi.tw.twks.cli.command.DumpCommand;
import edu.rpi.tw.twks.cli.command.PostNanopublicationsCommand;
import edu.rpi.tw.twks.client.TwksClient;
import edu.rpi.tw.twks.client.TwksClientConfiguration;
import edu.rpi.tw.twks.factory.TwksFactory;
import edu.rpi.tw.twks.factory.TwksFactoryConfiguration;
import org.apache.jena.query.ReadWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class CliMain {
    private final static Command[] commands = {
            new DeleteNanopublicationsCommand(),
            new DumpCommand(),
            new PostNanopublicationsCommand()
    };
    private final static Logger logger = LoggerFactory.getLogger(CliMain.class);

    public static void main(final String[] argv) {
        final JCommander.Builder jCommanderBuilder = JCommander.newBuilder();

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

        if (jCommander.getParsedCommand() == null) {
            jCommander.usage();
            return;
        }

        final Command command = commandsByName.get(jCommander.getParsedCommand());

        final Properties configurationProperties = new Properties();

        if (globalArgs.configurationFilePath != null) {
            try (final FileReader fileReader = new FileReader(new File(globalArgs.configurationFilePath))) {
                configurationProperties.load(fileReader);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        globalArgs.configuration.forEach((key, value) -> configurationProperties.setProperty(key, value));

        {
            final TwksFactoryConfiguration configuration = TwksFactoryConfiguration.builder().setFromSystemProperties().setFromProperties(configurationProperties).build();
            if (!configuration.isEmpty()) {
                final Twks twks = TwksFactory.getInstance().createTwks(configuration);
                logger.info("using library implementation {} with configuration {}", twks.getClass().getCanonicalName(), configuration);

                try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.READ)) {
                    command.run(new Command.Apis(transaction));
                }
                return;
            }
        }

        {
            final TwksClientConfiguration clientConfiguration = new TwksClientConfiguration();
            clientConfiguration.setFromSystemProperties().setFromProperties(configurationProperties);
            final TwksClient client = new TwksClient(clientConfiguration);
            logger.info("using client with configuration {}", clientConfiguration);

            command.run(new Command.Apis(client));
        }
    }

    private final static class GlobalArgs {
        @DynamicParameter(names = "-D", description = "library configuration, overrides -c and system properties")
        Map<String, String> configuration = new HashMap<>();

        @Parameter(names = {"-c"}, description = "library configuration file path in .properties format")
        String configurationFilePath;

        @Parameter(names = {"-h", "--help"})
        boolean help = false;
    }
}
