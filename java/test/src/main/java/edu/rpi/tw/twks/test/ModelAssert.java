package edu.rpi.tw.twks.test;

import edu.rpi.tw.twks.vocabulary.Vocabularies;
import org.apache.jena.rdf.model.Model;

import static org.junit.Assert.fail;

public final class ModelAssert {

    private ModelAssert() {
    }

    public static void assertModelEquals(final Model actualModel, final Model expectedModel) {
        if (actualModel.isIsomorphicWith(expectedModel)) {
            return;
        }
        printModelComparison(actualModel, expectedModel);
        fail();
    }

    //  private def toString(model: Model): String = {
    //    val writer = new StringWriter()
    //    model.write()
    //  }

    public static void printModelComparison(final Model actualModel, final Model expectedModel) {
        final String lang = "TURTLE";
        System.out.println("Expected model: ");
        expectedModel.write(System.out, lang);
        System.out.println();
        System.out.println("Actual model: ");
        actualModel.write(System.out, lang);
        System.out.println();
        System.out.println("Differences:");
        final Model difference = actualModel.difference(expectedModel);
        Vocabularies.setNsPrefixes(difference);
        difference.write(System.out, lang);
    }
}
