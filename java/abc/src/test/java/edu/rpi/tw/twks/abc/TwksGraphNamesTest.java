package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksTransaction;
import edu.rpi.tw.twks.test.TestData;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.jena.query.ReadWrite;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class TwksGraphNamesTest {
    private final TestData testData = new TestData();
    protected @Nullable
    TwksGraphNames sut;
    private Twks twks;

    protected abstract TwksGraphNames newSystemUnderTest();

    @Before
    public void setUp() {
        twks = new MemTwks();
        sut = newSystemUnderTest();
    }

    @Test
    public void testGetAllAssertionGraphNames() {
        twks.putNanopublication(testData.specNanopublication);
        try (final TwksTransaction transaction = twks.beginTransaction(ReadWrite.READ)) {
            assertEquals(ImmutableSet.of(testData.specNanopublication.getAssertion().getName()), sut.getAllAssertionGraphNames(transaction));
        }
    }
}
