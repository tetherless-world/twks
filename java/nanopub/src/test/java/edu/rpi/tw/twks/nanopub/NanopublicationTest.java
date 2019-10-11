package edu.rpi.tw.twks.nanopub;

import edu.rpi.tw.twks.test.TestData;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

public final class NanopublicationTest {
    private TestData testData;

    @Before
    public void setUp() throws Exception {
        this.testData = new TestData();
    }

    @Test
    public void testToDataset() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = NanopublicationFactory.getInstance().createNanopublicationFromDataset(testData.specNanopublicationDataset);
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
    public void testToDatasetDuplicateModelName() throws MalformedNanopublicationException {
        final Nanopublication nanopublication = NanopublicationFactory.getInstance().createNanopublicationFromDataset(testData.specNanopublicationDataset);
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
        } catch (final DuplicateModelNameException e) {
        }
    }
}
