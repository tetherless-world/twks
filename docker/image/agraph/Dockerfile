FROM franzinc/agraph:v6.6.0

USER agraph

COPY /docker/image/agraph/agraph.cfg /data/etc/agraph.cfg

RUN rm /entrypoint.sh
COPY /docker/image/agraph/entrypoint.sh /entrypoint.sh

EXPOSE 10000-10035
