# TWKS documentation

This documentation is hosted on [readthedocs.io](https://readthedocs.io).

It uses the Sphinx documentation system with reStructuredText. For documentation on how to develop this documentation, see [this page](https://docs.readthedocs.io/).

## Installing Sphinx

The current directory contains a `requirements.txt` that can be used to install Sphinx easily.

	cd doc
	python3 -m venv venv
	source venv/bin/activate
	pip install -r requirements.txt

Then you can run `make html` and other Sphinx targets.	
