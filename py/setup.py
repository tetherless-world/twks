from setuptools import setup

setup(
    name="twks-client",
    version="1.0.0",
    author="Tetherless World Constellation",
    license="Apache License 2.0",
    packages=['twks', 'twks.client'],
    install_requires=[
        'rdflib>=4',
        'SPARQLWrapper>=1,<2',
        'requests>=2,<3',
        'tw-nanopub>=1'
    ],
    classifiers=[
        "License :: OSI Approved :: Apache Software License",
    ],
)
