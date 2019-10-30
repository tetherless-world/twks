.. _py:

Using TWKS from Python
======================

Python prerequisites
--------------------

* `Python 3 <https://www.python.org/>`_

One-time Python setup
---------------------

Install the library:

.. code-block::

   cd py
   python3 setup.py install


Or add a dependency from PyPI: `twks-client <https://pypi.org/project/twks-client/>`_.


.. _py-client:

Using the Python client
-----------------------

`TwksClient <https://github.com/tetherless-world/twks/blob/master/py/twks/client/twks_client.py>`_ is the entry point class.

The client API mirrors that of the TWKS server:
* CRUD operations on nanopublications
* querying assertions and nanopublications via SPARQL

Python examples
---------------

The repository contains a `number of examples of using TWKS from Python <https://github.com/tetherless-world/twks/tree/master/py/examples>`_.
