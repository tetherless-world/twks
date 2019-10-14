package edu.rpi.tw.twks.uri;

import org.junit.Test;

import static org.junit.Assert.*;

public final class UriTest {
    @Test
    public void testParse() {
        Uri.parse("http://example.com");
        Uri.parse("urn:test:test");
    }

    @Test
    public void testToString() {
        assertEquals("http://example.com", Uri.parse("http://example.com").toString());
    }

    @Test
    public void testEquals() {
        final String value = "http://example.com";
        final Uri actual = Uri.parse(value);
        final Uri expected = Uri.parse(value);
        assertEquals(expected, actual);
        assertNotSame(expected, actual);
    }

    @Test
    public void testHashCode() {
        final Uri uri = Uri.parse("http://example.com");
        final int hashCode = uri.hashCode();
        assertNotEquals(System.identityHashCode(uri), hashCode);
    }
}
