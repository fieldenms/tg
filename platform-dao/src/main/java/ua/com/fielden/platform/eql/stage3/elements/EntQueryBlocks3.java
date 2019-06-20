package ua.com.fielden.platform.eql.stage3.elements;

import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.sources.Sources3;

public class EntQueryBlocks3 {
    public final Sources3 sources;
    public final Conditions3 conditions;

    public EntQueryBlocks3(final Sources3 sources, final Conditions3 conditions) {
        this.sources = sources;
        this.conditions = conditions;
    }
}