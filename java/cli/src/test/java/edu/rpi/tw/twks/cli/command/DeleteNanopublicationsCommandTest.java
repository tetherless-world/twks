package edu.rpi.tw.twks.cli.command;

import org.junit.Test;

public final class DeleteNanopublicationsCommandTest extends AbstractCommandTest<DeleteNanopublicationsCommand> {
    @Override
    protected DeleteNanopublicationsCommand newCommand() {
        return new DeleteNanopublicationsCommand();
    }

    @Test
    public void testDeleteAbsent() {
        command.getArgs().nanopublicationUris.add(getTestData().specNanopublication.getUri().toString());
    }

    @Test
    public void testDeletePresent() {
        getTwks().putNanopublication(getTestData().specNanopublication);
        command.getArgs().nanopublicationUris.add(getTestData().specNanopublication.getUri().toString());
    }
}
