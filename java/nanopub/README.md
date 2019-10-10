# Java nanopublication library

This library is an implementation of the current [Nanopublication Guidelines](http://nanopub.org/guidelines/working_draft/). It can be used independently of TWKS.

## Use

The [`Nanopublication`](src/main/java/edu/rpi/tw/twks/nanopub/Nanopublication.java) class is the primary abstraction. You can parse nanopublications or loose assertion graphs with the [`NanopublicationParser`](src/main/java/edu/rpi/tw/twks/nanopub/NanopublicationParser.java) class or build them from parts (named graphs) using the [`NanopublicationFactory`](src/main/java/edu/rpi/tw/twks/nanopub/NanopublicationFactory.java) class. 
