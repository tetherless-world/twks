import os.path

from setuptools import setup

with open(os.path.join(os.path.dirname(__file__), "README.md"), "r") as fh:
    long_description = fh.read()

setup(
    author="Tetherless World Constellation",
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
    ],
    description="Tetherless World Knowledge Store (TWKS) client",
    install_requires=[
        'rdflib>=4',
        'SPARQLWrapper>=1,<2',
        'requests>=2,<3',
    ],
    license="Apache License 2.0",
    long_description=long_description,
    long_description_content_type="text/markdown",
    name="twks-client",
    packages=['twks', 'twks.client', 'twks.nanopub'],
    url="https://github.com/tetherless-world/twks",
    version="1.0.0",
)
