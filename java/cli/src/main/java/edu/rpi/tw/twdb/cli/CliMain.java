package edu.rpi.tw.twdb.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import edu.rpi.tw.twdb.cli.command.Command;
import edu.rpi.tw.twdb.cli.command.PutCommand;

import java.util.HashMap;
import java.util.Map;

public final class CliMain {
    private final static Command[] commands = {
            new PutCommand()
    };

    public static void main(final String[] argv) {
        final JCommander.Builder jCommanderBuilder = JCommander.newBuilder();

        final GlobalArgs globalArgs = new GlobalArgs();
        jCommanderBuilder.addObject(globalArgs);

        // Commands
        final Map<String, Command> commandsByName = new HashMap<>();
        for (final Command command : commands) {
            jCommanderBuilder.addCommand(command.getName(), command.getArgs());
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

        command.run();
    }

    private final static class GlobalArgs {
        @Parameter(names = {"-h", "--help"})
        boolean help = false;
    }
}
