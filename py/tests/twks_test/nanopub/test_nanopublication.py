from twks.nanopub.nanopublication import Nanopublication


def test_parse(spec_nanopublication_trig_file_path):
    nanopublication = Nanopublication.parse(source=spec_nanopublication_trig_file_path)
    assert isinstance(nanopublication, Nanopublication)


def test_serialize(spec_nanopublication_trig_file_path):
    nanopublication = Nanopublication.parse(source=spec_nanopublication_trig_file_path)
    nanopublication_str = nanopublication.serialize()
    assert nanopublication_str
