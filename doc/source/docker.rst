Using TWKS from Docker
======================

Docker prerequisites
--------------------

1. `Docker <https://docs.docker.com/v17.12/install/>`_
2. `Docker Compose <https://docs.docker.com/compose/install/>`_


.. _docker-server:

Running the TWKS server in Docker
---------------------------------

::

    cd docker
    script/server

The Docker container exposes the server's REST and SPARQL APIs via HTTP on port 8080, which is bound to localhost.

The server Dockerfile has several declared VOLUMEs:

- ``/data``: where the server stores its data
- ``/extcp``: where the server loads `Service Provider Interface (SPI) <https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html>`_ .jars
- ``/extfs``: where the server loads extension scripts

The volumes can be accessed by running any other container with ``--volumes-from``. After starting up the server:

::

    docker run -it --volumes-from=twks-server ubuntu:bionic bash

which mounts the volumes at the paths listed above. You can add an additional bind mount of a host directory:

::

    docker run -it -v $PWD:/host --volumes-from=twks-server ubuntu:bionic bash

and then copy files from the host to the appropriate ``twks-server`` volume e.g., ``cp /host/my-spi.jar /extcp``.


.. _docker-cli:

Running the TWKS command line interface with Docker
---------------------------------------------------

Run the server as above, then:

::

    cd docker
    cat nanopublication.trig | docker-compose run -T twks-cli put-nanopublications --lang trig -

Explanation:

- ``docker-compose run twks-cli`` runs the TWKS command line interface container and connects it to the server.
- ``put-nanopublication`` is the CLI sub-command
- ``--lang trig`` specifies that the input will be in Trig format
- ``put-nanopublications`` reads from stdin since ``-`` was specified (a ``-f file`` would be expected to be in the container; it is easier to use stdin)
- ``cat nanopublication.trig`` writes the contents of that file to the container's stdin
