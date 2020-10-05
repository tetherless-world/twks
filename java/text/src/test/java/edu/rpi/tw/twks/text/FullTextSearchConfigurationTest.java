package edu.rpi.tw.twks.text;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;

public final class FullTextSearchConfigurationTest {
    @Test
    public void testBuild() {
        final FullTextSearchConfiguration configuration = FullTextSearchConfiguration.builder().build();
        assertFalse(configuration.getEnable());
        assertFalse(configuration.getLuceneDirectoryPath().isPresent());
    }

    @Test
    public void testSetEnable() {
        final FullTextSearchConfiguration configuration = FullTextSearchConfiguration.builder().setEnable(true).build();
        assertTrue(configuration.getEnable());
    }

    @Test
    public void testSetLuceneDirectoryPath() {
        final Optional<Path> value = Optional.of(Paths.get("/"));
        final FullTextSearchConfiguration configuration = FullTextSearchConfiguration.builder().setLuceneDirectoryPath(value).build();
        assertEquals(value, configuration.getLuceneDirectoryPath());
    }
}
