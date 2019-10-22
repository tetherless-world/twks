package edu.rpi.tw.twks.nanopub;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;

import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

public final class MoreDatasetFactory extends DatasetFactory {
    /**
     * Create a dataset from a SELECT result set which has the variables ?G ?S ?P ?O.
     * <p>
     * SPARQL 1.1 does not support CONSTRUCT WHERE { GRAPH ?G { ?S ?P ?O } }
     */
    public static Dataset createDatasetFromResultSet(final ResultSet resultSet) {
        final Dataset dataset = org.apache.jena.query.DatasetFactory.create();
        while (resultSet.hasNext()) {
            final QuerySolution querySolution = resultSet.nextSolution();
            final Resource g = querySolution.getResource("G");
            final RDFNode o = querySolution.get("O");
            final Property p = ResourceFactory.createProperty(querySolution.getResource("P").getURI());
            final Resource s = querySolution.getResource("S");

            Model model = dataset.getNamedModel(g.getURI());
            if (model == null) {
                model = ModelFactory.createDefaultModel();
                setNsPrefixes(model);
                dataset.addNamedModel(g.getURI(), model);
            }
            model.add(s, p, o);
        }
        return dataset;
    }

}
