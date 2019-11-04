FROM franzinc/agraph:v6.6.0

RUN yum -y install sudo

COPY --chown=agraph /docker/image/agraph/agraph.cfg /data/etc/agraph.cfg
# Copy in a license if we have one
COPY --chown=agraph /docker/image/agraph/license.txt /data/etc/license.txt
RUN bash -c "cat /data/etc/license.txt >>/data/etc/agraph.cfg"

RUN rm /entrypoint.sh
COPY /docker/image/agraph/entrypoint.sh /entrypoint.sh

EXPOSE 10000-10035
