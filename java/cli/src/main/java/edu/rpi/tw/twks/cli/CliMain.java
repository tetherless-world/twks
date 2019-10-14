package edu.rpi.tw.twks.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.cli.command.Command;
import edu.rpi.tw.twks.cli.command.PutNanopublicationsCommand;
import edu.rpi.tw.twks.client.TwksClient;
import edu.rpi.tw.twks.factory.TwksConfiguration;
import edu.rpi.tw.twks.factory.TwksFactory;
import org.apache.jena.query.ReadWrite;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class CliMain {
    private final static Command[] commands = {
            new PutNanopublicationsCommand()
    };

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
        for (final Enumeration<?> propertyNameI = System.getProperties().propertyNames(); propertyNameI.hasMoreElements(); ) {
            final String propertyName = (String) propertyNameI.nextElement();
            configurationProperties.setProperty(propertyName, System.getProperty(propertyName));
        }
        if (globalArgs.configurationFilePath != null) {
            try (final FileReader fileReader = new FileReader(new File(globalArgs.configurationFilePath))) {
                configurationProperties.load(fileReader);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        final TwksConfiguration configuration = new TwksConfiguration();
        configuration.setFromProperties(configurationProperties);
        if (!configuration.isEmpty()) {
            final Twks twks = TwksFactory.getInstance().createTwks(configuration);
            try (final TwksTransaction sparqlQueryTransaction = twks.beginTransaction(ReadWrite.READ)) {
                command.run(twks, sparqlQueryTransaction);
            }
        } else {
            final String baseUrlPropertyName = TwksClient.class.getPackage().getName() + "." + TwksClient.class.getSimpleName() + ".baseUrl";
        }
    }

    private final static class GlobalArgs {
        @Parameter(names = {"-h", "--help"})
        boolean help = false;

        @Parameter(names = {"-c"}, description = "library configuration file path in .properties format")
        String configurationFilePath;
    }
}
