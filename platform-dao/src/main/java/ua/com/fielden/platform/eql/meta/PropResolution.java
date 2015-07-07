package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s1.elements.EntProp1;
import ua.com.fielden.platform.eql.s2.elements.ISource2;

public class PropResolution {
    private final boolean aliased;
    private final ISource2 source;
    private final ResolutionPath resolution;
    private final EntProp1 entProp;

    public PropResolution(final boolean aliased, final ISource2 source, final ResolutionPath resolution, final EntProp1 entProp) {
        super();
        this.aliased = aliased;
        this.source = source;
        this.resolution = resolution;
        this.entProp = entProp;
    }

    public boolean isAliased() {
        return aliased;
    }

    public ISource2 getSource() {
        return source;
    }

    public ResolutionPath getResolution() {
        return resolution;
    }

    public EntProp1 getEntProp() {
        return entProp;
    }
}
