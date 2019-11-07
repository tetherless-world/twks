FROM franzinc/agraph:v6.6.0

COPY --chown=agraph /docker/image/agraph/agraph.cfg /data/etc/agraph.cfg

RUN rm /entrypoint.sh
COPY /docker/image/agraph/entrypoint.sh /entrypoint.sh

# ENTRYPOINT ["/bin/sh", "-c"]

EXPOSE 10000-10035
