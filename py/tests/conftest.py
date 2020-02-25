import os.path

import pytest
import rdflib
from rdflib import URIRef, RDF, OWL

from twks.nanopub.nanopublication import Nanopublication


@pytest.fixture
def assertions_ttl_file_path():
    return os.path.abspath(os.path.join(os.path.dirname(__file__), "assertions.ttl"))


@pytest.fixture
def ontology_nanopublication(ontology_uri, spec_nanopublication_trig_file_path):
    assertions_graph = rdflib.Graph()
    assertions_graph.parse(source=spec_nanopublication_trig_file_path, format="trig")
    assertions_graph.add((ontology_uri, RDF["type"], OWL["Ontology"]))
    return Nanopublication.from_assertions(assertions_graph)


@pytest.fixture
def ontology_uri():
    return URIRef("http://example.com/ontology")


@pytest.fixture
def spec_nanopublication_trig_file_path():
    return os.path.abspath(os.path.join(os.path.dirname(__file__), "spec_nanopublication.trig"))


@pytest.fixture
def spec_nanopublication(spec_nanopublication_trig_file_path):
    return Nanopublication.parse(source=spec_nanopublication_trig_file_path)
