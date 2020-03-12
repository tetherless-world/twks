from os import path

from setuptools import setup

description = "Tetherless World Knowledge Store (TWKS) client"
try:
    with open(path.abspath(path.join(path.dirname(__file__), "..", "README.md")), encoding='utf-8') as f:
        long_description = f.read()
except:
    long_description = description

setup(
    author="Tetherless World Constellation",
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: Apache Software License",
        "Operating System :: OS Independent",
    ],
    description=description,
    install_requires=[
        'rdflib>=4',
        'SPARQLWrapper>=1,<2',
        'requests>=2,<3',
    ],
    license="Apache License 2.0",
    long_description=long_description,
    long_description_content_type='text/markdown',
    name="twks-client",
    packages=['twks', 'twks.client', 'twks.nanopub'],
    url="https://github.com/tetherless-world/twks",
    version="1.0.4",
)
