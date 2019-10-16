def test_import():
    try:
        __import__("examples.extfs.put_nanopublication.put_nanopublication_example")
    except SystemExit:
        # Argument parsing will fail
        pass
