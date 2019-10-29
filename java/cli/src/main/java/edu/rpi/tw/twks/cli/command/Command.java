package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.api.*;
import edu.rpi.tw.twks.client.TwksClient;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Command {
    public String[] getAliases() {
        return new String[0];
    }

    public abstract Object getArgs();

    public abstract String getName();

    public abstract void run(Apis apis);

    public final static class Apis {
        private final BulkReadApi bulkReadApi;
        private final AdministrationApi bulkWriteApi;
        private final NanopublicationCrudApi nanopublicationCrudApi;
        private final QueryApi queryApi;

        public Apis(final TwksClient client) {
            this(client, client, client, client);
        }

        public Apis(final TwksTransaction transaction) {
            this(transaction, transaction, transaction, transaction);
        }

        private Apis(final BulkReadApi bulkReadApi, final AdministrationApi bulkWriteApi, final NanopublicationCrudApi nanopublicationCrudApi, final QueryApi queryApi) {
            this.bulkReadApi = checkNotNull(bulkReadApi);
            this.bulkWriteApi = checkNotNull(bulkWriteApi);
            this.nanopublicationCrudApi = checkNotNull(nanopublicationCrudApi);
            this.queryApi = checkNotNull(queryApi);
        }

        public final BulkReadApi getBulkReadApi() {
            return bulkReadApi;
        }

        public final AdministrationApi getBulkWriteApi() {
            return bulkWriteApi;
        }

        public final NanopublicationCrudApi getNanopublicationCrudApi() {
            return nanopublicationCrudApi;
        }

        public final QueryApi getQueryApi() {
            return queryApi;
        }
    }
}
