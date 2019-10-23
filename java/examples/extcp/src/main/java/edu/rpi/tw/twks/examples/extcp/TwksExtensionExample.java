package edu.rpi.tw.twks.examples.extcp;

import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksExtension;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public final class TwksExtensionExample implements TwksExtension {
    private final static Logger logger = LoggerFactory.getLogger(TwksExtensionExample.class);

    public TwksExtensionExample() {
        logger.info("instantiating example extension");
    }

    @Override
    public final void destroy() {
    }

    @Override
    public final void initialize(final Twks twks) {
        logger.info("initializing example extension");

        twks.registerChangeObserver(() -> {
            final Model assertions = twks.getAssertions();

            final byte[] assertionTurtle;
            {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                assertions.write(out, Lang.TURTLE.getName());
                assertionTurtle = out.toByteArray();
            }

            final OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
            final OWLOntology owlOntology;
            try {
                owlOntology = owlOntologyManager.loadOntologyFromOntologyDocument(new ByteArrayInputStream(assertionTurtle));
            } catch (final OWLOntologyCreationException e) {
                logger.error("error creating OWL ontology from assertions: ", e);
                return;
            }

            final OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(owlOntology);
            if (reasoner.isConsistent()) {
                logger.info("current assertions are consistent");
            } else {
                logger.info("current assertions are inconsistent");
            }
        });

        logger.info("initialized example extension");
    }
}
