package edu.rpi.tw.twks.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.cli.command.Command;
import edu.rpi.tw.twks.cli.command.PutNanopublicationsCommand;
import edu.rpi.tw.twks.lib.TwksConfiguration;
import edu.rpi.tw.twks.lib.TwksFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

        final Twks db;
        {
            final TwksConfiguration dbConfiguration = new TwksConfiguration();
            dbConfiguration.setFromSystemProperties();
            if (globalArgs.configurationFilePath != null) {
                final Properties properties = new Properties();
                try (final FileReader fileReader = new FileReader(new File(globalArgs.configurationFilePath))) {
                    properties.load(fileReader);
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
                dbConfiguration.setFromProperties(properties);
            }

            db = TwksFactory.getInstance().createTwdb(dbConfiguration);
        }

        command.run(db);
    }

    private final static class GlobalArgs {
        @Parameter(names = {"-h", "--help"})
        boolean help = false;

        @Parameter(names = {"-c"}, description = "configuration file path in .properties format")
        String configurationFilePath;
    }
}
