package edu.rpi.tw.twks.abc;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.observer.*;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.test.TestData;
import edu.rpi.tw.twks.uri.Uri;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class TwksObserversTest {
    private final TestData testData;
    private final Twks twks;
    private TwksObservers observers;

    public TwksObserversTest() throws Exception {
        testData = new TestData();
        twks = mock(Twks.class);
    }

    @Before
    public void setUp() {
        observers = new TwksObservers(twks);
    }

    @Test
    public void testOnChange() {
        final TestChangeObserver observer = new TestChangeObserver();
        final TwksObserverRegistration registration = observers.registerChangeObserver(observer);
        // onChange can't be invoked directly
        assertEquals(0, observer.invocationCount);
        observers.onDeleteNanopublication(testData.specNanopublication.getUri());
        assertEquals(1, observer.invocationCount);
        registration.unregister();
        observers.onDeleteNanopublication(testData.specNanopublication.getUri());
        assertEquals(1, observer.invocationCount);
    }

    @Test
    public void testOnDeleteNanopublication() {
        final TestDeleteNanopublicationObserver observer = new TestDeleteNanopublicationObserver();
        final TwksObserverRegistration registration = observers.registerDeleteNanopublicationObserver(observer);
        observers.onDeleteNanopublication(testData.specNanopublication.getUri());
        assertEquals(1, observer.deleteNanopublicationUris.size());
        assertEquals(testData.specNanopublication.getUri(), observer.deleteNanopublicationUris.get(0));
        registration.unregister();
        observers.onDeleteNanopublication(testData.secondNanopublication.getUri());
        assertEquals(1, observer.deleteNanopublicationUris.size());
        assertEquals(testData.specNanopublication.getUri(), observer.deleteNanopublicationUris.get(0));
    }

    @Test
    public void testOnDeleteNanopublicationAsynchronous() throws InterruptedException {
        final TestAsynchronousDeleteNanopublicationObserver observer = new TestAsynchronousDeleteNanopublicationObserver();
        final TwksObserverRegistration registration = observers.registerDeleteNanopublicationObserver(observer);
        assertTrue(observer.deleteNanopublicationUris.isEmpty());
        observers.onDeleteNanopublication(testData.specNanopublication.getUri());
        for (int tryI = 0; tryI < 100; tryI++) {
            if (!observer.deleteNanopublicationUris.isEmpty()) {
                break;
            }
            Thread.sleep(100);
        }
        assertEquals(1, observer.deleteNanopublicationUris.size());
        assertEquals(testData.specNanopublication.getUri(), observer.deleteNanopublicationUris.get(0));
    }

    @Test
    public void testOnPutNanopublication() {
        final TestPutNanopublicationObserver observer = new TestPutNanopublicationObserver();
        final TwksObserverRegistration registration = observers.registerPutNanopublicationObserver(observer);
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

    private static class TestDeleteNanopublicationObserver implements DeleteNanopublicationObserver {
        List<Uri> deleteNanopublicationUris = new ArrayList<>();

        @Override
        public void onDeleteNanopublication(final Uri nanopublicationUri) {
            deleteNanopublicationUris.add(nanopublicationUri);
        }
    }

    private static class TestAsynchronousDeleteNanopublicationObserver extends TestDeleteNanopublicationObserver implements AsynchronousTwksObserver {
    }

    private final static class TestChangeObserver implements ChangeObserver {
        int invocationCount = 0;

        @Override
        public void onChange() {
            invocationCount++;
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
