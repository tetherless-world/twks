import pytest

from twdb.client.twdb_client import TwdbClient


@pytest.fixture
def client():
    return TwdbClient()


def test_delete_nanopublication_absent(client, spec_nanopublication):
    client.delete_nanopublication(spec_nanopublication.uri)
    assert not client.delete_nanopublication(spec_nanopublication.uri)


def test_delete_nanopublication_present(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    assert client.delete_nanopublication(spec_nanopublication.uri)


def test_get_nanopublication_absent(client, spec_nanopublication):
    client.delete_nanopublication(spec_nanopublication.uri)
    actual_nanopublication = client.get_nanopublication(spec_nanopublication.uri)
    assert actual_nanopublication is None


def test_get_nanopublication_present(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
    actual_nanopublication = client.get_nanopublication(spec_nanopublication.uri)
    assert actual_nanopublication is not None
    assert actual_nanopublication.isomorphic(spec_nanopublication)


def test_put_nanopublication(client, spec_nanopublication):
    client.put_nanopublication(spec_nanopublication)
