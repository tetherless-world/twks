package edu.rpi.tw.twks.api;

import java.io.IOException;

public interface AdministrationApi {
    /**
     * Dump the contents of the store to the configured dump location.
     */
    void dump() throws IOException;
}
