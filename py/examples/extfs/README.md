# Example Python TWKS file system extensions (extfs)

## Running

This example can be run in different ways:
* Copying the contents of this directory into a `twks-server` Docker container's `/extfs` directory. See the Docker documentation for instructions on how to do that.
* Starting the server locally and adding `-Dtwks.extfs=/path/to/this/dir`

## Overview

The current directory has the structure that the `twks-server` expects in `/extfs`:
* `put_nanopublication/`: a subdirectory containing putNanopublication observer scripts
* `put_nanopublication/put_nanopublication_example.py`: observer in Python. This file is ignored by the server because it is not executable (`chmod +x`).
* `put_nanopublicationl/put_nanopublication_example.sh`: shell script that sets up the Python environment and executes the companion .py file. This file is considered the entrypoint by the server because it's executable.

The `__init__.py` files are not required by extfs. They are present for testing purposes.
