#!/bin/bash

MY_DIR_PATH=$(dirname "$0")

VENV_DIR_PATH="${MY_DIR_PATH}/../../../../venv"

if [ -d $VENV_DIR_PATH ]; then
  source $VENV_DIR_PATH/bin/activate
fi

export PYTHONPATH="${MY_DIR_PATH}/../../../.."

python3 $MY_DIR_PATH/put_nanopublication_example.py $*
