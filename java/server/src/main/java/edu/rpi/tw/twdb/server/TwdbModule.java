package edu.rpi.tw.twdb.server;

import com.google.inject.AbstractModule;
import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.lib.TwdbConfiguration;
import edu.rpi.tw.twdb.lib.TwdbFactory;

final class TwdbModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Twdb.class).toInstance(TwdbFactory.getInstance().createTwdb(new TwdbConfiguration().setFromSystemProperties()));
    }
}
