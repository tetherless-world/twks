#!/bin/bash

# Example TWKS extfs extension in Python.
# This file is marked executable, so extfs will pick it up. The file extension is not significant on Unix.
# The separate shell script is needed to set up the Python virtual environment correctly.
# extfs does not assume anything about the script, only that it is executable.

MY_DIR_PATH=$(dirname "$0")

VENV_DIR_PATH="${MY_DIR_PATH}/../../../venv"

if [ -d $VENV_DIR_PATH ]; then
  source $VENV_DIR_PATH/bin/activate
fi

export PYTHONPATH="${MY_DIR_PATH}/../../.."

python3 $MY_DIR_PATH/delete_nanopublication_example.py $*
