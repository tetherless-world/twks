TWKS Developer Guide
====================

Creating a release
^^^^^^^^^^^^^^^^^^

Java
~~~~

One-time setup
--------------

Java releases are `hosted on OSSRH <https://central.sonatype.org/pages/apache-maven.html>`_ and deployed with Maven. You will need an account on OSSRH that has access to the TWKS project. Using the credentials from your account, reate a file ``~/.m2/settings.xml``:

::

    <settings>
      <servers>
        <server>
          <id>ossrh</id>
          <username>YOURUSERNAME</username>
          <password>YOURPASSWORD</password>
        </server>
      </servers>
    </settings>

Performing a release
--------------------

There are two scripts for Java releases, which must be run in the following order on the ``master`` branch:

::

    cd java
    ./prepare_release.sh
    ./perform_release.sh

The scripts use standard Maven commands to prepare a release (tagging and committing the released commit GitHub), push it to OSSRH, and update all ``pom.xml`` files to the next snapshot version.

The Java example projects are not tied directly to the TWKS parent ``pom.xml``. You will need to do a global find-and-replace of ``OLDVERSION-SNAPSHOT`` with ``NEWVERSION-SNAPSHOT`` after you have called the scripts above but before you push the commits to ``master``. For example, replace ``1.0.0-SNAPSHOT`` with ``1.0.1-SNAPSHOT``.

Python
~~~~~~

One-time setup
--------------

Python releases are `hosted on the Python Package Index (PyPI) <https://pypi.org/>`_. You will need to create a PyPI account.

Performing a release
--------------------

There is a single script to perform a Python release:

::

    cd py
    ./release.sh

Which uses the standard ``setup.py`` and ``twine`` to build source and binary releases and upload them to PyPI. The latter process will request your PyPI credentials.

After performing the release, update the version in ``setup.py`` to the next version that will be released.

