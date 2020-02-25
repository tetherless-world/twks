import os

import pytest
from rdflib import URIRef

from twks.client.twks_client import TwksClient


@pytest.fixture
def client(ontology_nanopublication, spec_nanopublication):
    client = TwksClient(server_base_url=os.environ.get("TWKS_SERVER_BASE_URL", None))
    yield client
    client.delete_nanopublication(ontology_nanopublication.uri)
    client.delete_nanopublication(spec_nanopublication.uri)


def test_delete_nanopublication_absent(client, spec_nanopublication):
    client.delete_nanopublication(spec_nanopublication.uri)
    assert not client.delete_nanopublication(spec_nanopublication.uri)


def test_delete_nanopublication_present(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    assert client.delete_nanopublication(spec_nanopublication.uri)


def test_dump(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    client.dump()
    # Can't tell what the server has done


def test_get_assertions(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    assertions = client.get_assertions()
    assert len(assertions) == 1


def test_get_nanopublication_absent(client, spec_nanopublication):
    client.delete_nanopublication(spec_nanopublication.uri)
    actual_nanopublication = client.get_nanopublication(spec_nanopublication.uri)
    assert actual_nanopublication is None


def test_get_nanopublication_present(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    actual_nanopublication = client.get_nanopublication(spec_nanopublication.uri)
    assert actual_nanopublication is not None
    assert actual_nanopublication.isomorphic(spec_nanopublication)


def test_get_ontology_assertions(client, ontology_nanopublication, ontology_uri, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    assertions = client.get_ontology_assertions(frozenset((ontology_uri,)))
    assert len(assertions) == 0

    client.put_nanopublication(ontology_nanopublication)
    assertions = client.get_ontology_assertions(frozenset((ontology_uri,)))
    # for assertion in assertions:
    #     print(assertion)
    assert len(assertions) == 2  # The assertion from the assertions file plus the rdf:type owl:Ontology


def test_put_nanopublication(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)


def test_query_assertions(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    graph = client.query_assertions("CONSTRUCT WHERE { ?s ?p ?o }").graph
    assert len(graph) == 1
    s, p, o = tuple(graph.triples((None, None, None)))[0]
    assert s == URIRef("http://example.org/trastuzumab")
    assert p == URIRef("http://example.org/is-indicated-for")
    assert o == URIRef("http://example.org/breast-cancer")


def test_query_nanopublications(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    rows = tuple(client.query_nanopublications(
        "SELECT ?s ?p ?o WHERE { GRAPH ?H { ?np <http://www.nanopub.org/nschema#hasAssertion> ?A } GRAPH ?A { ?s ?p ?o } }"))
    assert len(rows) == 1
