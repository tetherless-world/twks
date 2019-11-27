package edu.rpi.tw.twks.abc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import edu.rpi.tw.twks.api.QueryApi;
import edu.rpi.tw.twks.api.TwksGraphNameCacheConfiguration;
import edu.rpi.tw.twks.uri.Uri;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

final class CachinglTwksGraphNames implements TwksGraphNames {
    private final TwksGraphNameCacheConfiguration configuration;
    private final TwksGraphNames delegate;
    private final Cache<Uri, ImmutableSet<Uri>> nanopublicationGraphNamesCache;
    private final Cache<Uri, ImmutableSet<Uri>> ontologyAssertionGraphNamesCache;
    private @Nullable
    ImmutableSet<Uri> allAssertionGraphNames = null;

    CachinglTwksGraphNames(final TwksGraphNameCacheConfiguration configuration, final TwksGraphNames delegate) {
        this.configuration = checkNotNull(configuration);
        checkState(configuration.getEnable());
        this.delegate = checkNotNull(delegate);

        nanopublicationGraphNamesCache = CacheBuilder.newBuilder().build();
        ontologyAssertionGraphNamesCache = CacheBuilder.newBuilder().build();
    }

    @Override
    public ImmutableSet<Uri> getAllAssertionGraphNames(final QueryApi queryApi) {
        synchronized (this) {
            // Use this to synchronize the assignment
            // More obvious than putting it in the method declaration.
            if (allAssertionGraphNames == null) {
                allAssertionGraphNames = delegate.getAllAssertionGraphNames(queryApi);
            }
            return allAssertionGraphNames;
        }
    }

    @Override
    public ImmutableSet<Uri> getNanopublicationGraphNames(final Uri nanopublicationUri, final QueryApi queryApi) {
        try {
            return nanopublicationGraphNamesCache.get(nanopublicationUri, () -> delegate.getNanopublicationGraphNames(nanopublicationUri, queryApi));
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ImmutableSet<Uri> getOntologyAssertionGraphNames(final ImmutableSet<Uri> ontologyUris, final QueryApi queryApi) {
        final ImmutableSet.Builder<Uri> resultBuilder = ImmutableSet.builder();
        for (final Uri ontologyUri : ontologyUris) {
            try {
                resultBuilder.addAll(ontologyAssertionGraphNamesCache.get(ontologyUri, () -> delegate.getOntologyAssertionGraphNames(ImmutableSet.of(ontologyUri), queryApi)));
            } catch (final ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return resultBuilder.build();
    }

    @Override
    public void invalidateCache() {
        synchronized (this) {
            allAssertionGraphNames = null;
        }
        nanopublicationGraphNamesCache.invalidateAll();
        ontologyAssertionGraphNamesCache.invalidateAll();
    }
}

