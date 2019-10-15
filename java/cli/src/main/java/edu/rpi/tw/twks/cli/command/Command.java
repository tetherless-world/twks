package edu.rpi.tw.twks.cli.command;

import edu.rpi.tw.twks.api.BulkReadApi;
import edu.rpi.tw.twks.api.NanopublicationCrudApi;
import edu.rpi.tw.twks.api.QueryApi;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class Command {
    public abstract Object getArgs();

    public String[] getAliases() {
        return new String[0];
    }

    public abstract String getName();

    public abstract void run(Apis apis);

    public final static class Apis {
        private final BulkReadApi bulkReadApi;
        private final NanopublicationCrudApi nanopublicationCrudApi;
        private final QueryApi queryApi;

        public Apis(final BulkReadApi bulkReadApi, final NanopublicationCrudApi nanopublicationCrudApi, final QueryApi queryApi) {
            this.bulkReadApi = checkNotNull(bulkReadApi);
            this.nanopublicationCrudApi = checkNotNull(nanopublicationCrudApi);
            this.queryApi = checkNotNull(queryApi);
        }

        public final BulkReadApi getBulkReadApi() {
            return bulkReadApi;
        }

        public final NanopublicationCrudApi getNanopublicationCrudApi() {
            return nanopublicationCrudApi;
        }

        public final QueryApi getQueryApi() {
            return queryApi;
        }
    }
}
