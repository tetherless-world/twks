package edu.rpi.tw.twdb.api;

import org.apache.jena.rdf.model.Model;

public interface NanopublicationParser {
    Nanopublication parseNanopublication(Model model);
}
