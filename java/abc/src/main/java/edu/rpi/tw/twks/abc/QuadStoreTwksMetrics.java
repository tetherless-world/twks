package edu.rpi.tw.twks.abc;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import static com.codahale.metrics.MetricRegistry.name;

public final class QuadStoreTwksMetrics extends AbstractTwksMetrics {
    public final Timer getNanopublicationGraphNamesTimer;
    public final Timer putNanopublicationAddNamedGraphsTimer;
    public final Timer putNanopublicationUpdateAllAssertionsUnionGraphTimer;
    public final Timer putNanopublicationUpdateOntologyAssertionsUnionGraphTimer;

    public QuadStoreTwksMetrics(final MetricRegistry registry) {
        super(registry);
        getNanopublicationGraphNamesTimer = registry.timer(name(getClass(), "getNanopublicationGraphNamesTimer"));
        putNanopublicationAddNamedGraphsTimer = registry.timer(name(getClass(), "putNanopublicationAddNamedGraphsTimer"));
        putNanopublicationUpdateAllAssertionsUnionGraphTimer = registry.timer(name(getClass(), "putNanopublicationUpdateAllAssertionsUnionGraphTimer"));
        putNanopublicationUpdateOntologyAssertionsUnionGraphTimer = registry.timer(name(getClass(), "putNanopublicationUpdateOntologyAssertionsUnionGraphTimer"));
    }
}
