package edu.rpi.tw.twks.ext;

import com.google.common.collect.ImmutableList;
import edu.rpi.tw.twks.api.Twks;
import edu.rpi.tw.twks.api.TwksExtension;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class ClasspathExtensions extends AbstractExtensions {
    private final ImmutableList<TwksExtension> extensions;

    public ClasspathExtensions(final Twks twks) {
        super(twks);

        final ServiceLoader<TwksExtension> serviceLoader = ServiceLoader.load(TwksExtension.class);
        final ImmutableList.Builder<TwksExtension> extensionsBuilder = ImmutableList.builder();
        for (final Iterator<TwksExtension> extensionI = serviceLoader.iterator(); extensionI.hasNext(); ) {
            extensionsBuilder.add(extensionI.next());
        }
        extensions = extensionsBuilder.build();
    }

    @Override
    public final void destroy() {
        extensions.forEach(extensions -> extensions.destroy());
    }

    @Override
    public final void initialize() {
        extensions.forEach(extension -> extension.initialize(getTwks()));
    }
}
