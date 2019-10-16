package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.api.observer.DeleteNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.PutNanopublicationTwksObserver;
import edu.rpi.tw.twks.api.observer.TwksObserverRegistration;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.test.TestData;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.ReadWrite;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class TwksObserversTest {
    private final TestData testData;
    private final TestTwks twks;
    private TwksObservers observers;

    public TwksObserversTest() throws Exception {
        testData = new TestData();
        twks = new TestTwks();
    }

    @Before
    public void setUp() {
        observers = new TwksObservers(twks);
    }

    @Test
    public void testOnDeleteNanopublication() {
        final TestDeleteNanopublicationObserver observer = new TestDeleteNanopublicationObserver();
        final TwksObserverRegistration registration = observers.registerDeleteNanopublicationObserver(observer);
        observers.onDeleteNanopublication(twks, testData.specNanopublication.getUri());
        assertEquals(1, observer.deleteNanopublicationUris.size());
        assertEquals(testData.specNanopublication.getUri(), observer.deleteNanopublicationUris.get(0));
        registration.unregister();
        observers.onDeleteNanopublication(twks, testData.secondNanopublication.getUri());
        assertEquals(1, observer.deleteNanopublicationUris.size());
        assertEquals(testData.specNanopublication.getUri(), observer.deleteNanopublicationUris.get(0));
    }

    @Test
    public void testOnPutNanopublication() {
        final TestPutNanopublicationObserver observer = new TestPutNanopublicationObserver();
        final TwksObserverRegistration registration = observers.registerPutNanopublicationObserver(observer);
        observers.onPutNanopublication(twks, testData.specNanopublication);
        assertEquals(1, observer.putNanopublications.size());
        assertEquals(testData.specNanopublication.getUri(), observer.putNanopublications.get(0).getUri());
        registration.unregister();
        observers.onPutNanopublication(twks, testData.secondNanopublication);
        assertEquals(1, observer.putNanopublications.size());
        assertEquals(testData.specNanopublication.getUri(), observer.putNanopublications.get(0).getUri());
    }

    @Test
    public void testRegisterDeleteNanopublicationObserver() {
        observers.registerDeleteNanopublicationObserver(new TestDeleteNanopublicationObserver());
    }

    @Test
    public void testRegisterPutNanopublicationObserver() {
        observers.registerPutNanopublicationObserver(new TestPutNanopublicationObserver());
    }

    @Test
    public void testUnregisterDeleteNanopublicationObserver() {
        observers.registerDeleteNanopublicationObserver(new TestDeleteNanopublicationObserver()).unregister();
    }

    @Test
    public void testUnregisterPutNanopublicationObserver() {
        observers.registerPutNanopublicationObserver(new TestPutNanopublicationObserver()).unregister();
    }

    private final static class TestTwks extends AbstractTwks {
        @Override
        protected TwksTransaction _beginTransaction(final ReadWrite readWrite) {
            throw new UnsupportedOperationException();
        }
    }

    private final static class TestDeleteNanopublicationObserver implements DeleteNanopublicationTwksObserver {
        List<Uri> deleteNanopublicationUris = new ArrayList<>();

        @Override
        public void onDeleteNanopublication(final Twks twks, final Uri nanopublicationUri) {
            deleteNanopublicationUris.add(nanopublicationUri);
        }
    }

    private final static class TestPutNanopublicationObserver implements PutNanopublicationTwksObserver {
        List<Nanopublication> putNanopublications = new ArrayList<>();

        @Override
        public void onPutNanopublication(final Twks twks, final Nanopublication nanopublication) {
            putNanopublications.add(nanopublication);
        }
    }
}
