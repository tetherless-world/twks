Server API
==========

The TWKS [HTTP] server exposes a variety of APIs for manipulating nanopublications and querying the store. The following sections will describe the APIs in detail.

URLs are shown as paths only. A path ``/path`` would be equivalent to the URL ``http://localhost:8080/path``.

OpenAPI Schema
--------------

The server exports an `OpenAPI <https://www.openapis.org/>`_ schema of its interfaces at

::

    /openapi.json

OpenAPI Schema Request
~~~~~~~~~~~~~~~~~~~~~~

::

    curl http://localhost:8080/openapi.json

OpenAPI Schema Response
~~~~~~~~~~~~~~~~~~~~~~~

.. code-block:: json

    {
      "openapi": "3.0.1",
      "paths": {
        "/assertions": {
          "get": {
            "summary": "Get all assertions in the store",
            "operationId": "getAssertions",
            "parameters": [
              {
                "name": "Accept",
                "in": "header",
                "schema": {
                  "type": "string"
                }
              }
            ],
            "responses": {
              "default": {
                "description": "default response",
                "content": {
                  "*/*": {}
                }
              }
            }
          }
        }
      }
    }


