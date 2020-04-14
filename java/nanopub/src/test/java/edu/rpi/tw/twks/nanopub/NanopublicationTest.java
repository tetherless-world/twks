package edu.rpi.tw.twks.nanopub;

import com.google.common.base.Charsets;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

public final class NanopublicationTest {
    private NanopublicationParser parser;
    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.parser = NanopublicationParser.builder().setDialect(NanopublicationDialect.SPECIFICATION).setLang(Lang.TRIG).build();
        this.testData = new TestData();
    }

    @Test
    public void testBuilder() {
        assertNotSame(null, Nanopublication.builder());
    }

    @Test
    public void testToDataset() {
        final Nanopublication nanopublication = parser.parseDataset(testData.specNanopublicationDataset).get(0);
        {
            final Dataset actual = nanopublication.toDataset();
            assertTrue(actual.getUnionModel().isIsomorphicWith(testData.specNanopublicationDataset.getUnionModel()));
        }
        {
            final Dataset actual = DatasetFactory.create();
            nanopublication.toDataset(actual);
            assertTrue(actual.getUnionModel().isIsomorphicWith(testData.specNanopublicationDataset.getUnionModel()));
        }
    }

    @Test
    public void testToDatasetDuplicateModelName() {
        final Nanopublication nanopublication = parser.parseDataset(testData.specNanopublicationDataset).get(0);
        final Dataset actual = DatasetFactory.create();
        {
            // containsModel will fail if the model is empty
            final Model dummyModel = ModelFactory.createDefaultModel();
            dummyModel.add(dummyModel.createResource("http://example.com"), RDF.type, dummyModel.createResource("http://example.com/Type"));
            actual.addNamedModel(nanopublication.getHead().getName().toString(), dummyModel);
        }
        try {
            nanopublication.toDataset(actual);
            fail();
        } catch (final DuplicateNanopublicationPartName e) {
        }
    }

    @Test
    public void testWrite() throws Exception {
        final Nanopublication expected = parser.parseFile(testData.specNanopublicationFilePath).get(0);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        expected.write(bos);
        final String actualString = new String(bos.toByteArray(), Charsets.UTF_8);
        final Nanopublication actual = parser.parseString(actualString).get(0);
        Assert.assertTrue(expected.isIsomorphicWith(actual));
    }

    @Test
    public void testWriteToString() throws Exception {
        final Nanopublication expected = parser.parseFile(testData.specNanopublicationFilePath).get(0);
        final String actualString = expected.writeToString();
        final Nanopublication actual = parser.parseString(actualString).get(0);
        Assert.assertTrue(expected.isIsomorphicWith(actual));
    }
}
