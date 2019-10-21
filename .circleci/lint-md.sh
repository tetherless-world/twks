#!/bin/bash

set +e

mkdir lint-results

sudo gem install --silent mdl
find . -name README.md | xargs mdl >>lint-results/mdl.txt 2>&1

sudo npm install -g --silent markdown-link-check
find . -name README.md | xargs markdown-link-check >>lint-results/markdown-lint-check 2>&1
