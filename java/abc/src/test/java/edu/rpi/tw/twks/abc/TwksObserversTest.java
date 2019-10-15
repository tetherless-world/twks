package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.observer.DeleteNanopublicationObserver;
import edu.rpi.tw.twks.api.observer.ObserverRegistration;
import edu.rpi.tw.twks.api.observer.PutNanopublicationObserver;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.test.TestData;
import edu.rpi.tw.twks.uri.Uri;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class TwksObserversTest {
    private final TestData testData;
    private TwksObservers observers;

    public TwksObserversTest() throws Exception {
        testData = new TestData();
    }

    @Before
    public void setUp() {
        observers = new TwksObservers();
    }

    @Test
    public void testOnDeleteNanopublication() {
        final TestDeleteNanopublicationObserver observer = new TestDeleteNanopublicationObserver();
        final ObserverRegistration registration = observers.registerDeleteNanopublicationObserver(observer);
        observers.onDeleteNanopublication(testData.specNanopublication.getUri());
        assertEquals(1, observer.deleteNanopublicationUris.size());
        assertEquals(testData.specNanopublication.getUri(), observer.deleteNanopublicationUris.get(0));
        registration.unregister();
        observers.onDeleteNanopublication(testData.secondNanopublication.getUri());
        assertEquals(1, observer.deleteNanopublicationUris.size());
        assertEquals(testData.specNanopublication.getUri(), observer.deleteNanopublicationUris.get(0));
    }

    @Test
    public void testOnPutNanopublication() {
        final TestPutNanopublicationObserver observer = new TestPutNanopublicationObserver();
        final ObserverRegistration registration = observers.registerPutNanopublicationObserver(observer);
        observers.onPutNanopublication(testData.specNanopublication);
        assertEquals(1, observer.putNanopublications.size());
        assertEquals(testData.specNanopublication.getUri(), observer.putNanopublications.get(0).getUri());
        registration.unregister();
        observers.onPutNanopublication(testData.secondNanopublication);
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

    private final static class TestDeleteNanopublicationObserver implements DeleteNanopublicationObserver {
        List<Uri> deleteNanopublicationUris = new ArrayList<>();

        @Override
        public void onDeleteNanopublication(final Uri nanopublicationUri) {
            deleteNanopublicationUris.add(nanopublicationUri);
        }
    }

    private final static class TestPutNanopublicationObserver implements PutNanopublicationObserver {
        List<Nanopublication> putNanopublications = new ArrayList<>();

        @Override
        public void onPutNanopublication(final Nanopublication nanopublication) {
            putNanopublications.add(nanopublication);
        }
    }
}
