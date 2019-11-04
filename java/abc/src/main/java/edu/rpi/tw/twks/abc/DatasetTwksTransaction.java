package edu.rpi.tw.twks.abc;

import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.TwksConfiguration;
import edu.rpi.tw.twks.nanopub.AutoCloseableIterable;
import edu.rpi.tw.twks.nanopub.DatasetTransaction;
import edu.rpi.tw.twks.nanopub.Nanopublication;
import edu.rpi.tw.twks.nanopub.NanopublicationFactory;
import edu.rpi.tw.twks.uri.Uri;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.rpi.tw.twks.vocabulary.Vocabularies.setNsPrefixes;

/**
 * A TwksTransaction that wraps a DatasetTransaction.
 */
public abstract class DatasetTwksTransaction extends AbstractTwksTransaction {
    private final static Logger logger = LoggerFactory.getLogger(DatasetTwksTransaction.class);

    private final Dataset dataset;
    private final DatasetTransaction datasetTransaction;

    protected DatasetTwksTransaction(final TwksConfiguration configuration, final Dataset dataset, final ReadWrite readWrite) {
        super(configuration);
        this.dataset = checkNotNull(dataset);
        this.datasetTransaction = new DatasetTransaction(dataset, readWrite);
    }

    @Override
    public final void abort() {
        datasetTransaction.abort();
    }

    @Override
    public final void close() {
        datasetTransaction.close();
    }

    @Override
    public final void commit() {
        datasetTransaction.commit();
    }

    @Override
    public final DeleteNanopublicationResult deleteNanopublication(final Uri uri) {
        final Set<String> nanopublicationGraphNames = getNanopublicationGraphNames(uri);
        if (nanopublicationGraphNames.isEmpty()) {
            return DeleteNanopublicationResult.NOT_FOUND;
        }
        if (nanopublicationGraphNames.size() != 4) {
            throw new IllegalStateException();
        }
        for (final String nanopublicationGraphName : nanopublicationGraphNames) {
            getDataset().removeNamedModel(nanopublicationGraphName);
        }
        return DeleteNanopublicationResult.DELETED;
    }

    @Override
    public final Model getAssertions() {
        final Set<String> assertionGraphNames = getAssertionGraphNames();
        final Model assertions = ModelFactory.createDefaultModel();
        if (assertionGraphNames.isEmpty()) {
            return assertions;
        }
        setNsPrefixes(assertions);
        for (final String assertionGraphName : assertionGraphNames) {
            final Model assertion = getDataset().getNamedModel(assertionGraphName);
            assertions.add(assertion);
        }
        return assertions;
    }

    protected final Dataset getDataset() {
        return dataset;
    }

    public final DatasetTransaction getDatasetTransaction() {
        return datasetTransaction;
    }

    @Override
    public final Model getOntologyAssertions(final ImmutableSet<Uri> ontologyUris) {
        final Model assertions = ModelFactory.createDefaultModel();
        setNsPrefixes(assertions);
        for (final Uri ontologyUri : ontologyUris) {
            for (final String assertionGraphName : getOntologyAssertionGraphNames(ontologyUri)) {
                final Model assertion = getDataset().getNamedModel(assertionGraphName);
                assertions.add(assertion);
            }
        }
        return assertions;
    }

    @Override
    protected final AutoCloseableIterable<Nanopublication> iterateNanopublications() {
        return new NanopublicationFactory().iterateNanopublicationsFromDataset(getDataset(), getDatasetTransaction());
    }

    @Override
    public final PutNanopublicationResult putNanopublication(final Nanopublication nanopublication) {
        final DeleteNanopublicationResult deleteResult = deleteNanopublication(nanopublication.getUri());
        nanopublication.toDataset(getDataset(), getDatasetTransaction());
        return deleteResult == DeleteNanopublicationResult.DELETED ? PutNanopublicationResult.OVERWROTE : PutNanopublicationResult.CREATED;
    }

    @Override
    public final QueryExecution queryNanopublications(final Query query) {
        return QueryExecutionFactory.create(query, getDataset());
    }
}
