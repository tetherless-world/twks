from setuptools import setup

setup(
    name="twdb-client",
    version="1.0.0",
    author="Tetherless World Constellation",
    license="Apache License 2.0",
    packages=['twdb'],
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
