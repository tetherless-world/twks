package edu.rpi.tw.twks.agraph;

import edu.rpi.tw.twks.abc.AbstractTwks;
import edu.rpi.tw.twks.api.TwksTransaction;
import org.apache.jena.query.ReadWrite;

public final class AllegroGraphTwks extends AbstractTwks<AllegroGraphTwksConfiguration> {
    public AllegroGraphTwks(final AllegroGraphTwksConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected TwksTransaction _beginTransaction(final ReadWrite readWrite) {
        return new AllegroGraphTwksTransaction((AllegroGraphTwksConfiguration) getConfiguration(), readWrite);
    }
}
