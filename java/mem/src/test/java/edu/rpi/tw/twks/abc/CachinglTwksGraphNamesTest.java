package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.TwksGraphNameCacheConfiguration;
import edu.rpi.tw.twks.mem.MemTwks;
import edu.rpi.tw.twks.mem.MemTwksConfiguration;

public final class CachinglTwksGraphNamesTest extends TwksGraphNamesTest {
    @Override
    protected MemTwks newTwks() {
        return new MemTwks(MemTwksConfiguration.builder().setGraphNameCacheConfiguration(TwksGraphNameCacheConfiguration.builder().setEnable(true).build()).build());
    }
}
