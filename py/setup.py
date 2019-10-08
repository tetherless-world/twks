from setuptools import setup

setup(
    name="twdb-client",
    version="1.0.0",
    author="Tetherless World Constellation",
    license="Apache License 2.0",
    packages=['twdb'],
    install_requires=[
        'rdflib',
        'tw-nanopub'
    ],
    classifiers=[
        "License :: OSI Approved :: Apache Software License",
    ],
)
