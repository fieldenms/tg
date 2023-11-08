package ua.com.fielden.platform.eql.stage2.sources.enhance;

import java.util.List;

public class AbstractLinks<T> {
    public final List<Prop2Lite> links;
    public final T resolution;

    public AbstractLinks(final List<Prop2Lite> links, final T resolution) {
        this.links = links;
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +" [links = " + links +", resolution = " + resolution + "]";
    }
}