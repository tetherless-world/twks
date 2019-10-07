package edu.rpi.tw.twdb.server.servlet.nanopublication;

import edu.rpi.tw.twdb.api.Twdb;
import edu.rpi.tw.twdb.server.TestData;
import edu.rpi.tw.twdb.server.servlet.AbstractHttpServletTest;

public final class NanopublicationHttpServletTest extends AbstractHttpServletTest<NanopublicationHttpServlet> {
    @Override
    protected NanopublicationHttpServlet _setUp(final Twdb db, final TestData testData) {
        return new NanopublicationHttpServlet(db);
    }
}
