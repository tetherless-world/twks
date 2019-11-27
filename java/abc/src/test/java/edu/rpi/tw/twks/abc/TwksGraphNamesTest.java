package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.test.TestData;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.ReadWrite;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class TwksGraphNamesTest {
    private final TestData testData = new TestData();
    protected @Nullable
    TwksGraphNames sut;
    private MemTwks twks;

    protected abstract MemTwks newTwks();

    @Before
    public void setUp() {
        twks = newTwks();
        sut = twks.getGraphNames();
    }

    @Test
    public void testGetAllAssertionGraphNames() {
        twks.putNanopublication(testData.specNanopublication);
        for (int i = 0; i < 2; i++) { // Test caching
            try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.READ)) {
                assertEquals(ImmutableSet.of(testData.specNanopublication.getAssertion().getName()), sut.getAllAssertionGraphNames(transaction));
            }
        }
        twks.putNanopublication(testData.secondNanopublication);
        for (int i = 0; i < 2; i++) { // Test caching
            try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.READ)) {
                assertEquals(ImmutableSet.of(testData.specNanopublication.getAssertion().getName(), testData.secondNanopublication.getAssertion().getName()), sut.getAllAssertionGraphNames(transaction));
            }
        }
    }

    @Test
    public void testGetNanopublicationGraphNames() {
        twks.putNanopublication(testData.specNanopublication);
        for (int i = 0; i < 2; i++) { // Test caching
            try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.READ)) {
                final ImmutableSet<Uri> actual = sut.getNanopublicationGraphNames(testData.specNanopublication.getUri(), transaction);
                assertEquals(4, actual.size());
                // Don't have access to the nanopublication head URI, just test contains
                assertTrue(actual.containsAll(ImmutableList.of(testData.specNanopublication.getAssertion().getName(), testData.specNanopublication.getProvenance().getName(), testData.specNanopublication.getPublicationInfo().getName())));
            }
        }
    }

    @Test
    public void testGetOntologyAssertionGraphNames() {
        twks.postNanopublications(ImmutableList.of(testData.ontologyNanopublication, testData.secondOntologyNanopublication));
        for (int i = 0; i < 2; i++) { // Test caching
            try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.READ)) {
                final ImmutableSet<Uri> actual = sut.getOntologyAssertionGraphNames(ImmutableSet.of(testData.ontologyUri, testData.secondOntologyUri), transaction);
                assertEquals(ImmutableSet.of(testData.ontologyNanopublication.getAssertion().getName(), testData.secondOntologyNanopublication.getAssertion().getName()), actual);
            }
        }
    }
}
