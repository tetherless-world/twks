package edu.rpi.tw.twdb.server.servlet.nanopublication;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.rpi.tw.twdb.api.Twdb;

import javax.servlet.http.HttpServlet;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
class NanopublicationHttpServlet extends HttpServlet {
    private final Twdb db;

    @Inject
    public NanopublicationHttpServlet(final Twdb db) {
        this.db = checkNotNull(db);
    }


}
